package io.smallrye.reactive.converters.rxjava1;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.assertj.core.util.Arrays;
import rx.Observable;
import rx.Single;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import io.smallrye.reactive.converters.Registry;
import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SingleConverterTest {

    private static final String ONE_VALUE = "hello";
    private static final String EXCEPTION_EXPECTED = "Exception expected";
    private ReactiveTypeConverter<Single> converter;

    @Before
    public void lookup() {
        converter = Registry.lookup(Single.class)
                .orElseThrow(() -> new AssertionError("Single converter should be found"));
    }

    @Test
    public void testToPublisherWithImmediateValue() {
        Single<String> single = Single.just(ONE_VALUE);
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(single));
        String res = flowable.blockingFirst();
        assertThat(res).isEqualTo(ONE_VALUE);
    }

    @Test
    public void testToPublisherWithDelayedValue() {
        Single<String> single = Single.just(ONE_VALUE).delay(10, TimeUnit.MILLISECONDS);
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(single));
        String res = flowable.blockingFirst();
        assertThat(res).isEqualTo(ONE_VALUE);
    }

    @Test
    public void testToPublisherWithImmediateFailure() {
        Single<String> single = Single.error(new BoomException("BOOM"));
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(single));
        try {
            //noinspection ResultOfMethodCallIgnored
            flowable.blockingFirst();
            fail(EXCEPTION_EXPECTED);
        } catch (BoomException e) {
            assertThat(e).hasMessage("BOOM");
        }
    }

    @Test
    public void testToPublisherWithDelayedFailure() {
        Single<String> single = Single.just(ONE_VALUE)
                .delay(10, TimeUnit.MILLISECONDS)
                .map(x -> {
                    throw new BoomException("BOOM");
                });
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(single));
        try {
            //noinspection ResultOfMethodCallIgnored
            flowable.blockingFirst();
            fail(EXCEPTION_EXPECTED);
        } catch (BoomException e) {
            assertThat(e).hasMessage("BOOM");
        }
    }

    @Test(expected = NullPointerException.class)
    public void testToPublisherWithDelayedNullValue() {
        Single<String> single = Single.just("goo").delay(10, TimeUnit.MILLISECONDS).map(s -> null);
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(single));
        //noinspection ResultOfMethodCallIgnored
        flowable.blockingFirst();
    }

    @Test(expected = NullPointerException.class)
    public void testToPublisherWithNullValue() {
        @SuppressWarnings("ConstantConditions") Single<String> single = Single.just(null);
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(single));
        //noinspection ResultOfMethodCallIgnored
        flowable.blockingFirst();
    }

    @Test
    public void testToPublisherWithStreamNotEmitting() throws InterruptedException {
        Single<String> single = Observable.<String>never().toSingle();
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(single));
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            //noinspection ResultOfMethodCallIgnored
            flowable.blockingFirst();
            latch.countDown();
        }).start();
        assertThat(latch.await(10, TimeUnit.MILLISECONDS)).isFalse();
    }

    @Test
    public void testFromCompletionStageWithImmediateValue() {
        AtomicReference<String> reference = new AtomicReference<>();
        @SuppressWarnings("unchecked")
        Single<String> single = converter
                .fromCompletionStage(CompletableFuture.completedFuture(ONE_VALUE))
                .cast(String.class);
        assertThat(single
                .doOnSuccess(reference::set)
                .toBlocking().value()).isEqualTo(ONE_VALUE);
        assertThat(reference).hasValue(ONE_VALUE);
    }

    @Test
    public void testFromCompletionStageWithDelayedValue() {
        AtomicReference<String> reference = new AtomicReference<>();
        @SuppressWarnings("unchecked")
        Single<String> future = converter
                .fromCompletionStage(CompletableFuture.supplyAsync(() -> ONE_VALUE))
                .cast(String.class);
        assertThat(future
                .doOnSuccess(reference::set)
                .toBlocking().value()).isEqualTo(ONE_VALUE);
        assertThat(reference).hasValue(ONE_VALUE);
    }

    @Test
    public void testFromCompletionStageWithImmediateFailure() {
        AtomicReference<Throwable> reference = new AtomicReference<>();
        CompletableFuture<String> future = new CompletableFuture<>();
        @SuppressWarnings("unchecked")
        Single<String> single = converter
                .fromCompletionStage(future)
                .cast(String.class);
        future.completeExceptionally(new BoomException("BOOM"));
        try {
            //noinspection ResultOfMethodCallIgnored
            single
                    .doOnError(reference::set)
                    .toBlocking().value();
            fail(EXCEPTION_EXPECTED);
        } catch (BoomException e) {
            assertThat(e).hasMessage("BOOM");
        }
        assertThat(reference.get()).isInstanceOf(BoomException.class).hasMessage("BOOM");
    }

    @Test
    public void testFromCompletionStageWithDelayedFailure() {
        AtomicReference<Throwable> reference = new AtomicReference<>();
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            throw new BoomException("BOOM");
        });

        @SuppressWarnings("unchecked")
        Single<String> single = converter
                .fromCompletionStage(future)
                .cast(String.class);
        try {
            //noinspection ResultOfMethodCallIgnored
            single
                    .doOnError(reference::set)
                    .toBlocking().value();
            fail(EXCEPTION_EXPECTED);
        } catch (CompletionException e) {
            assertThat(e.getCause())
                    .isInstanceOf(BoomException.class)
                    .hasMessage("BOOM");
        }
        assertThat(reference.get()).isInstanceOf(CompletionException.class).hasMessageContaining("BOOM");
    }

    @Test
    public void testFromCompletionStageWithNullValue() {
        CompletionStage<Void> future = CompletableFuture.completedFuture(null);
        @SuppressWarnings("unchecked")
        Single<String> single = converter
                .fromCompletionStage(future);
        //noinspection ResultOfMethodCallIgnored
        String val = single.toBlocking().value();
        assertThat(val).isNull();
    }

    @Test
    public void testFromCompletionStageWithDelayedNullValue() {
        CompletionStage<Void> future = CompletableFuture.supplyAsync(() -> null);
        @SuppressWarnings("unchecked")
        Single<String> single = converter
                .fromCompletionStage(future);
        //noinspection ResultOfMethodCallIgnored
        String val = single.toBlocking().value();
        assertThat(val).isNull();
    }

    @Test
    public void testFromCompletionStageThatGetCancelled() {
        CompletableFuture<String> future = new CompletableFuture<>();
        @SuppressWarnings("unchecked")
        Single<String> single = converter
                .fromCompletionStage(future)
                .cast(String.class);
        future.cancel(false);
        try {
            //noinspection ResultOfMethodCallIgnored
            single.toBlocking().value();
            fail(EXCEPTION_EXPECTED);
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(CancellationException.class);
        }
    }

    @Test
    public void testFromCompletionStageWithoutRedeemingAValue() throws InterruptedException {
        CompletionStage<String> never = new CompletableFuture<>();
        @SuppressWarnings("unchecked") Single<String> single = converter.fromCompletionStage(never);
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            //noinspection ResultOfMethodCallIgnored
            single.toBlocking().value();
            latch.countDown();
        }).start();
        assertThat(latch.await(10, TimeUnit.MILLISECONDS)).isFalse();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingOneImmediateValue() {
        Single<?> single = converter.fromPublisher(Flowable.just(ONE_VALUE));
        String o = single
                .cast(String.class)
                .toBlocking().value();
        assertThat(o).isEqualTo(ONE_VALUE);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingOneDelayedValue() {
        Single<?> single = converter.fromPublisher(Flowable.just(ONE_VALUE).delay(10, TimeUnit.MILLISECONDS));
        String o = single
                .cast(String.class)
                .toBlocking().value();
        assertThat(o).isEqualTo(ONE_VALUE);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BoomException.class)
    public void testFromPublisherEmittingAnImmediateFailure() {
        Single<?> single = converter.fromPublisher(Flowable.error(new BoomException("BOOM")));
        //noinspection ResultOfMethodCallIgnored
        single.cast(String.class).toBlocking().value();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BoomException.class)
    public void testFromPublisherEmittingAnDelayedFailure() {
        Single<?> single = converter.fromPublisher(Flowable.just(ONE_VALUE)
                .delay(10, TimeUnit.MILLISECONDS))
                .map(x -> {
                    throw new BoomException("BOOM");
                });
        //noinspection ResultOfMethodCallIgnored
        single.cast(String.class).toBlocking().value();
    }

    @Test(expected = NoSuchElementException.class)
    public void testFromEmptyPublisher() {
        Single<?> single = converter.fromPublisher(Flowable.empty());
        //noinspection ResultOfMethodCallIgnored
        single.toBlocking().value();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingImmediateMultipleValue() {
        Single<?> single = converter.fromPublisher(Flowable.just("h", "e", "l", "l", "o"));
        String o = single
                .cast(String.class)
                .toBlocking().value();
        assertThat(o).isEqualTo("h");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingDelayedMultipleValue() {
        Single<?> single = converter.fromPublisher(Flowable.just("h", "e", "l", "l", "o")
            .observeOn(Schedulers.computation())
        );
        String o = single
                .cast(String.class)
                .toBlocking().value();
        assertThat(o).isEqualTo("h");
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Test(expected = NullPointerException.class)
    public void testFromPublisherEmittingANullValueImmediately() {
        Single<String> single = converter.fromPublisher(Flowable.just(null));
        single.toBlocking().value();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void testFromPublisherEmittingADelayedNullValue() {
        Single<String> single = converter.fromPublisher(Flowable.just(ONE_VALUE).delay(10, TimeUnit.MILLISECONDS)
            .map(x -> null)
        );
        single.toBlocking().value();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherThatIsNeverEmitting() throws InterruptedException {
        Single<?> single = converter.fromPublisher(Flowable.never());

        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            //noinspection ResultOfMethodCallIgnored
            single.toBlocking().value();
            latch.countDown();
        }).start();
        assertThat(latch.await(10, TimeUnit.MILLISECONDS)).isFalse();
    }



}