package io.smallrye.reactive.converters.rxjava2;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import io.smallrye.reactive.converters.Registry;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class MaybeConverterTest {

    private ReactiveTypeConverter<Maybe> converter;

    @Before
    public void lookup() {
        converter = Registry.lookup(Maybe.class)
                .orElseThrow(() -> new AssertionError("Maybe converter should be found"));
    }

    @Test
    public void testToCompletionStageWithImmediateValue() {
        Maybe<String> maybe = Maybe.just("hello");
        CompletionStage<Optional<String>> stage = converter.toCompletionStage(maybe);
        Optional<String> res = stage.toCompletableFuture().join();
        assertThat(res).contains("hello");
    }

    @Test
    public void testToCompletionStageWithDelayedValue() {
        Maybe<String> maybe = Maybe.just("hello").delay(10, TimeUnit.MILLISECONDS);
        CompletionStage<Optional<String>> stage = converter.toCompletionStage(maybe);
        Optional<String> res = stage.toCompletableFuture().join();
        assertThat(res).contains("hello");
    }

    @Test
    public void testToCompletionStageWithImmediateCompletion() {
        Maybe<String> maybe = Maybe.empty();
        CompletionStage<Optional<String>> stage = converter.toCompletionStage(maybe);
        Optional<String> res = stage.toCompletableFuture().join();
        assertThat(res).isEmpty();
    }

    @Test
    public void testToCompletionStageWithDelayedCompletion() {
        Maybe<String> maybe = Maybe.<String>empty().delay(10, TimeUnit.MILLISECONDS);
        CompletionStage<Optional<String>> stage = converter.toCompletionStage(maybe);
        Optional<String> res = stage.toCompletableFuture().join();
        assertThat(res).isEmpty();
    }

    @Test
    public void testToCompletionStageWithImmediateFailure() {
        Maybe<String> maybe = Maybe.error(new BoomException("BOOM"));
        CompletionStage<String> stage = converter.toCompletionStage(maybe);
        try {
            stage.toCompletableFuture().join();
            fail("Exception expected");
        } catch (CompletionException e) {
            assertThat(e.getCause()).isInstanceOf(BoomException.class);
        }
    }

    @Test
    public void testToCompletionStageWithDelayedFailure() {
        Maybe<String> maybe = Maybe.just("hello")
                .delay(10, TimeUnit.MILLISECONDS)
                .map(x -> {
                    throw new BoomException("BOOM");
                });
        CompletionStage<String> stage = converter.toCompletionStage(maybe);
        try {
            stage.toCompletableFuture().join();
            fail("Exception expected");
        } catch (CompletionException e) {
            assertThat(e.getCause()).isInstanceOf(BoomException.class);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testToCompletionStageWithNullValue() {
        @SuppressWarnings("ConstantConditions") Maybe<String> maybe = Maybe.just(null);
        CompletionStage<String> stage = converter.toCompletionStage(maybe);
        stage.toCompletableFuture().join();
    }

    @Test
    public void testToCompletionStageWithDelayedNullValue() {
        Maybe<String> maybe = Maybe.just("goo").delay(10, TimeUnit.MILLISECONDS).map(s -> null);
        CompletionStage<String> stage = converter.toCompletionStage(maybe);
        try {
            stage.toCompletableFuture().join();
            fail("Exception expected");
        } catch (CompletionException e) {
            assertThat(e).hasCauseInstanceOf(NullPointerException.class);
        }
    }

    @Test
    public void testToCompletionStageWithStreamNotEmitting() {
        Maybe<String> maybe = Maybe.never();
        CompletionStage<String> stage = converter.toCompletionStage(maybe);
        try {
            stage.toCompletableFuture().get(10, TimeUnit.MILLISECONDS);
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(TimeoutException.class);
        }
    }

    @Test
    public void testToPublisherWithImmediateValue() {
        Maybe<String> maybe = Maybe.just("hello");
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(maybe));
        String res = flowable.blockingFirst();
        assertThat(res).isEqualTo("hello");
    }

    @Test
    public void testToPublisherWithDelayedValue() {
        Maybe<String> maybe = Maybe.just("hello").delay(10, TimeUnit.MILLISECONDS);
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(maybe));
        String res = flowable.blockingFirst();
        assertThat(res).isEqualTo("hello");
    }

    @Test
    public void testToPublisherWithImmediateFailure() {
        Maybe<String> maybe = Maybe.error(new BoomException("BOOM"));
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(maybe));
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
        Maybe<String> maybe = Maybe.just("hello")
                .delay(10, TimeUnit.MILLISECONDS)
                .map(x -> {
                    throw new BoomException("BOOM");
                });
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(maybe));
        try {
            //noinspection ResultOfMethodCallIgnored
            flowable.blockingFirst();
            fail("Exception expected");
        } catch (BoomException e) {
            assertThat(e).hasMessage("BOOM");
        }
    }

    @Test(expected = NullPointerException.class)
    public void testToPublisherWithNullValue() {
        @SuppressWarnings("ConstantConditions") Maybe<String> maybe = Maybe.just(null);
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(maybe));
        //noinspection ResultOfMethodCallIgnored
        flowable.blockingFirst();
    }

    @Test(expected = NullPointerException.class)
    public void testToPublisherWithDelayedNullValue() {
        Maybe<String> maybe = Maybe.just("goo").delay(10, TimeUnit.MILLISECONDS).map(s -> null);
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(maybe));
        //noinspection ResultOfMethodCallIgnored
        flowable.blockingFirst();
    }

    @Test
    public void testToPublisherWithStreamNotEmitting() throws InterruptedException {
        Maybe<String> maybe = Maybe.never();
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(maybe));
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
        Maybe<String> maybe = converter
                .fromCompletionStage(CompletableFuture.completedFuture("hello"))
                .cast(String.class);
        assertThat(maybe
                .doOnSuccess(reference::set)
                .blockingGet()).isEqualTo("hello");
        assertThat(reference).hasValue("hello");
    }

    @Test
    public void testFromCompletionStageWithDelayedValue() {
        AtomicReference<String> reference = new AtomicReference<>();
        @SuppressWarnings("unchecked")
        Maybe<String> future = converter
                .fromCompletionStage(CompletableFuture.supplyAsync(() -> "hello"))
                .cast(String.class);
        assertThat(future
                .doOnSuccess(reference::set)
                .blockingGet()).isEqualTo("hello");
        assertThat(reference).hasValue("hello");
    }

    @Test
    public void testFromCompletionStageWithImmediateFailure() {
        AtomicReference<Throwable> reference = new AtomicReference<>();
        CompletableFuture<String> future = new CompletableFuture<>();
        @SuppressWarnings("unchecked")
        Maybe<String> maybe = converter
                .fromCompletionStage(future)
                .cast(String.class);
        future.completeExceptionally(new BoomException("BOOM"));
        try {
            //noinspection ResultOfMethodCallIgnored
            maybe
                    .doOnError(reference::set)
                    .blockingGet();
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
        Maybe<String> maybe = converter
                .fromCompletionStage(future)
                .cast(String.class);
        try {
            //noinspection ResultOfMethodCallIgnored
            maybe
                    .doOnError(reference::set)
                    .blockingGet();
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
        Maybe<String> maybe = converter
                .fromCompletionStage(future);
        String res = maybe.blockingGet();
        assertThat(res).isNull();
    }

    @Test
    public void testFromCompletionStageWithDelayedNullValue() {
        CompletionStage<Void> future = CompletableFuture.supplyAsync(() -> null);
        @SuppressWarnings("unchecked")
        Maybe<String> maybe = converter
                .fromCompletionStage(future);
        String s = maybe.blockingGet();
        assertThat(s).isNull();
    }

    @Test
    public void testFromCompletionStageThatGetCancelled() {
        CompletableFuture<String> future = new CompletableFuture<>();
        @SuppressWarnings("unchecked")
        Maybe<String> maybe = converter
                .fromCompletionStage(future)
                .cast(String.class);
        future.cancel(false);
        try {
            //noinspection ResultOfMethodCallIgnored
            maybe.blockingGet();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(CancellationException.class);
        }
    }

    @Test
    public void testFromCompletionStageWithoutRedeemingAValue() throws InterruptedException {
        CompletionStage<String> never = new CompletableFuture<>();
        @SuppressWarnings("unchecked") Maybe<String> maybe = converter.fromCompletionStage(never);
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            //noinspection ResultOfMethodCallIgnored
            maybe.blockingGet();
            latch.countDown();
        }).start();
        assertThat(latch.await(10, TimeUnit.MILLISECONDS)).isFalse();
    }

    // ---------


    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingOneImmediateValue() {
        Maybe<?> maybe = converter.fromPublisher(Flowable.just("hello"));
        String o = maybe
                .cast(String.class)
                .blockingGet();
        assertThat(o).isEqualTo("hello");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingOneDelayedValue() {
        Maybe<?> maybe = converter.fromPublisher(Flowable.just("hello").delay(10, TimeUnit.MILLISECONDS));
        String o = maybe
                .cast(String.class)
                .blockingGet();
        assertThat(o).isEqualTo("hello");
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BoomException.class)
    public void testFromPublisherEmittingAnImmediateFailure() {
        Maybe<?> maybe = converter.fromPublisher(Flowable.error(new BoomException("BOOM")));
        //noinspection ResultOfMethodCallIgnored
        maybe.cast(String.class).blockingGet();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BoomException.class)
    public void testFromPublisherEmittingAnDelayedFailure() {
        Maybe<?> maybe = converter.fromPublisher(Flowable.just("hello")
                .delay(10, TimeUnit.MILLISECONDS))
                .map(x -> {
                    throw new BoomException("BOOM");
                });
        //noinspection ResultOfMethodCallIgnored
        maybe.cast(String.class).blockingGet();
    }

    @Test
    public void testFromEmptyPublisher() {
        @SuppressWarnings("unchecked") Maybe<String> maybe = converter.fromPublisher(Flowable.empty());
        String s = maybe.blockingGet();
        assertThat(s).isNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingImmediateMultipleValue() {
        Maybe<?> maybe = converter.fromPublisher(Flowable.just("h", "e", "l", "l", "o"));
        String o = maybe
                .cast(String.class)
                .blockingGet();
        assertThat(o).isEqualTo("h");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingDelayedMultipleValue() {
        Maybe<?> maybe = converter.fromPublisher(Flowable.just("h", "e", "l", "l", "o")
                .observeOn(Schedulers.computation())
        );
        String o = maybe
                .cast(String.class)
                .blockingGet();
        assertThat(o).isEqualTo("h");
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void testFromPublisherEmittingANullValueImmediately() {
        @SuppressWarnings("ConstantConditions") Maybe<?> maybe = converter.fromPublisher(Flowable.just(null));
        //noinspection ResultOfMethodCallIgnored
        maybe.cast(String.class).blockingGet();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void testFromPublisherEmittingADelayedNullValue() {
        Maybe<?> maybe = converter.fromPublisher(Flowable.just("hello").delay(10, TimeUnit.MILLISECONDS)
                .map(x -> null)
        );
        //noinspection ResultOfMethodCallIgnored
        maybe.cast(String.class).blockingGet();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherThatIsNeverEmitting() throws InterruptedException {
        Maybe<?> maybe = converter.fromPublisher(Flowable.never());

        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            //noinspection ResultOfMethodCallIgnored
            maybe.blockingGet();
            latch.countDown();
        }).start();
        assertThat(latch.await(10, TimeUnit.MILLISECONDS)).isFalse();
    }


}