package io.smallrye.reactive.converters.rxjava1;

import io.reactivex.Flowable;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import io.smallrye.reactive.converters.Registry;
import org.junit.Before;
import org.junit.Test;
import rx.Completable;
import rx.Observable;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CompletableConverterTest {

    private ReactiveTypeConverter<Completable> converter;

    @Before
    public void lookup() {
        converter = Registry.lookup(Completable.class)
                .orElseThrow(() -> new AssertionError("Completable converter should be found"));
    }

    @Test
    public void testToPublisherWithImmediateCompletion() {
        Completable completable = Completable.complete();
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(completable));
        String res = flowable.blockingFirst("DEFAULT");
        assertThat(res).isEqualTo("DEFAULT");
    }

    @Test
    public void testToPublisherWithDelayedCompletion() {
        Completable completable = Observable.just("hello").delay(10, TimeUnit.MILLISECONDS).toCompletable();
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(completable));
        String res = flowable.blockingFirst("DEFAULT");
        assertThat(res).isEqualTo("DEFAULT");
    }

    @Test
    public void testToPublisherWithImmediateFailure() {
        Completable completable = Completable.error(new BoomException("BOOM"));
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(completable));
        try {
            //noinspection ResultOfMethodCallIgnored
            flowable.blockingFirst();
            fail("Exception expected");
        } catch (BoomException e) {
            assertThat(e).hasMessage("BOOM");
        }
    }

    @Test
    public void testToPublisherWithDelayedFailure() {
        Completable completable = Observable.just("hello")
                .delay(10, TimeUnit.MILLISECONDS)
                .map(x -> {
                    throw new BoomException("BOOM");
                })
                .toCompletable();
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(completable));
        try {
            //noinspection ResultOfMethodCallIgnored
            flowable.blockingFirst();
            fail("Exception expected");
        } catch (BoomException e) {
            assertThat(e).hasMessage("BOOM");
        }
    }

    @Test
    public void testToPublisherWithStreamNotEmitting() throws InterruptedException {
        Completable completable = Completable.fromObservable(Observable.never());
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(completable));
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
        AtomicBoolean reference = new AtomicBoolean();
        @SuppressWarnings("unchecked")
        Completable completable = converter
                .fromCompletionStage(CompletableFuture.completedFuture("hello"));
        completable
                .doOnCompleted(() -> reference.set(true))
                .await();
        assertThat(reference).isTrue();
    }

    @Test
    public void testFromCompletionStageWithImmediateCompletion() {
        AtomicBoolean reference = new AtomicBoolean();
        @SuppressWarnings("unchecked")
        Completable completable = converter
                .fromCompletionStage(CompletableFuture.runAsync(() -> {
                }));
        completable
                .doOnCompleted(() -> reference.set(true))
                .await();
        assertThat(reference).isTrue();
    }

    @Test
    public void testFromCompletionStageWithDelayedValue() {
        AtomicBoolean reference = new AtomicBoolean();
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "hello");
        @SuppressWarnings("unchecked")
        Completable completable = converter
                .fromCompletionStage(future);
        completable
                .doOnCompleted(() -> reference.set(true))
                .await();
        assertThat(reference).isTrue();
    }

    @Test
    public void testFromCompletionStageWithDelayedCompletion() {
        AtomicBoolean reference = new AtomicBoolean();
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        });
        @SuppressWarnings("unchecked")
        Completable completable = converter
                .fromCompletionStage(future);
        completable
                .doOnCompleted(() -> reference.set(true))
                .await();
        assertThat(reference).isTrue();
    }

    @Test
    public void testFromCompletionStageWithImmediateFailure() {
        AtomicReference<Throwable> reference = new AtomicReference<>();
        CompletableFuture<String> future = new CompletableFuture<>();
        @SuppressWarnings("unchecked")
        Completable completable = converter
                .fromCompletionStage(future);
        future.completeExceptionally(new BoomException("BOOM"));
        try {
            completable
                    .doOnError(reference::set)
                    .await();
            fail("Exception expected");
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
        Completable completable = converter
                .fromCompletionStage(future);
        try {
            completable
                    .doOnError(reference::set)
                    .await();
            fail("Exception expected");
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
        Completable completable = converter
                .fromCompletionStage(future);
        AtomicBoolean reference = new AtomicBoolean();
        completable
                .doOnCompleted(() -> reference.set(true))
                .await();
        assertThat(reference).isTrue();
    }

    @Test
    public void testFromCompletionStageWithDelayedNullValue() {
        CompletionStage<Void> future = CompletableFuture.supplyAsync(() -> null);
        @SuppressWarnings("unchecked")
        Completable completable = converter
                .fromCompletionStage(future);
        AtomicBoolean reference = new AtomicBoolean();
        completable
                .doOnCompleted(() -> reference.set(true))
                .await();
        assertThat(reference).isTrue();
    }

    @Test
    public void testFromCompletionStageThatGetCancelled() {
        CompletableFuture<String> future = new CompletableFuture<>();
        @SuppressWarnings("unchecked")
        Completable completable = converter
                .fromCompletionStage(future);
        future.cancel(false);
        try {
            completable.await();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(CancellationException.class);
        }
    }

    @Test
    public void testFromCompletionStageWithoutRedeemingAValue() throws InterruptedException {
        CompletionStage<String> never = new CompletableFuture<>();
        @SuppressWarnings("unchecked") Completable completable = converter.fromCompletionStage(never);
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            completable.await();
            latch.countDown();
        }).start();
        assertThat(latch.await(10, TimeUnit.MILLISECONDS)).isFalse();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingOneImmediateValue() {
        AtomicBoolean reference = new AtomicBoolean();
        Completable completable = converter.fromPublisher(Flowable.just("hello"));
        completable
                .doOnCompleted(() -> reference.set(true))
                .await();
        assertThat(reference).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingOneDelayedValue() {
        AtomicBoolean reference = new AtomicBoolean();
        Completable completable = converter.fromPublisher(Flowable.just("hello").delay(10, TimeUnit.MILLISECONDS));
        completable
                .doOnCompleted(() -> reference.set(true))
                .await();
        assertThat(reference).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BoomException.class)
    public void testFromPublisherEmittingAnImmediateFailure() {
        Completable completable = converter.fromPublisher(Flowable.error(new BoomException("BOOM")));
        completable.await();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BoomException.class)
    public void testFromPublisherEmittingAnDelayedFailure() {
        Completable completable = converter.fromPublisher(Flowable.just("hello")
                .delay(10, TimeUnit.MILLISECONDS))
                .doOnCompleted(() -> {
                    throw new BoomException("BOOM");
                });
        completable.await();
    }

    @Test
    public void testFromEmptyPublisher() {
        AtomicBoolean reference = new AtomicBoolean();
        Completable completable = converter.fromPublisher(Flowable.empty());
        completable
                .doOnCompleted(() -> reference.set(true))
                .await();
        assertThat(reference).isTrue();

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingImmediateMultipleValue() {
        AtomicBoolean reference = new AtomicBoolean();
        Completable completable = converter.fromPublisher(Flowable.just("h", "e", "l", "l", "o"));
        completable
                .doOnCompleted(() -> reference.set(true))
                .await();
        assertThat(reference).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingDelayedMultipleValue() {
        AtomicBoolean reference = new AtomicBoolean();
        Completable completable = converter.fromPublisher(Flowable.just("h", "e", "l", "l", "o"))
                .observeOn(rx.schedulers.Schedulers.computation());
        completable
                .doOnCompleted(() -> reference.set(true))
                .await();
        assertThat(reference).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void testFromPublisherEmittingANullValueImmediately() {
        @SuppressWarnings("ConstantConditions") Completable completable = converter.fromPublisher(Flowable.just(null));
        completable.await();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void testFromPublisherEmittingADelayedNullValue() {
        Completable completable = converter.fromPublisher(Flowable.just("hello").delay(10, TimeUnit.MILLISECONDS)
                .map(x -> null)
        );
        completable.await();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherThatIsNeverEmitting() throws InterruptedException {
        Completable completable = converter.fromPublisher(Flowable.never());
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            //noinspection ResultOfMethodCallIgnored
            completable.await();
            latch.countDown();
        }).start();
        assertThat(latch.await(10, TimeUnit.MILLISECONDS)).isFalse();
    }


}