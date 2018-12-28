package io.smallrye.reactive.converters.rxjava2;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import io.smallrye.reactive.converters.Registry;
import org.junit.Before;
import org.junit.Test;

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
    public void testToCompletionStageWithImmediateCompletion() {
        Completable completable = Completable.complete();
        CompletionStage<Void> stage = converter.toCompletionStage(completable);
        Void res = stage.toCompletableFuture().join();
        assertThat(res).isNull();
    }

    @Test
    public void testToCompletionStageWithDelayedCompletion() {
        Completable completable = Single.just("hello").delay(10, TimeUnit.MILLISECONDS).ignoreElement();
        CompletionStage<Void> stage = converter.toCompletionStage(completable);
        Void res = stage.toCompletableFuture().join();
        assertThat(res).isNull();
    }

    @Test
    public void testToCompletionStageWithImmediateFailure() {
        Completable completable = Completable.error(new BoomException("BOOM"));
        CompletionStage<Void> stage = converter.toCompletionStage(completable);
        try {
            stage.toCompletableFuture().join();
            fail("Exception expected");
        } catch (CompletionException e) {
            assertThat(e.getCause()).isInstanceOf(BoomException.class);
        }
    }

    @Test
    public void testToCompletionStageWithDelayedFailure() {
        Completable completable = Single.just("hello")
                .delay(10, TimeUnit.MILLISECONDS)
                .map(x -> {
                    throw new BoomException("BOOM");
                })
                .ignoreElement();
        CompletionStage<Void> stage = converter.toCompletionStage(completable);
        try {
            stage.toCompletableFuture().join();
            fail("Exception expected");
        } catch (CompletionException e) {
            assertThat(e.getCause()).isInstanceOf(BoomException.class);
        }
    }

    @Test
    public void testToCompletionStageWithStreamNotEmitting() {
        Completable completable = Completable.fromPublisher(Flowable.never());
        CompletionStage<Void> stage = converter.toCompletionStage(completable);
        try {
            stage.toCompletableFuture().get(10, TimeUnit.MILLISECONDS);
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(TimeoutException.class);
        }
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
        Completable completable = Single.just("hello").delay(10, TimeUnit.MILLISECONDS).ignoreElement();
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
        Completable completable = Single.just("hello")
                .delay(10, TimeUnit.MILLISECONDS)
                .map(x -> {
                    throw new BoomException("BOOM");
                })
                .ignoreElement();
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
        Completable completable = Completable.fromPublisher(Flowable.never());
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
                .doOnComplete(() -> reference.set(true))
                .blockingAwait();
        assertThat(reference).isTrue();
    }

    @Test
    public void testFromCompletionStageWithImmediateCompletion() {
        AtomicBoolean reference = new AtomicBoolean();
        @SuppressWarnings("unchecked")
        Completable completable = converter
                .fromCompletionStage(CompletableFuture.runAsync(() -> {}));
        completable
                .doOnComplete(() -> reference.set(true))
                .blockingAwait();
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
                .doOnComplete(() -> reference.set(true))
                .blockingAwait();
        assertThat(reference).isTrue();
    }

    @Test
    public void testFromCompletionStageWithDelayedCompletion() {
        AtomicBoolean reference = new AtomicBoolean();
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {});
        @SuppressWarnings("unchecked")
        Completable completable = converter
                .fromCompletionStage(future);
        completable
                .doOnComplete(() -> reference.set(true))
                .blockingAwait();
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
                    .blockingAwait();
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
                    .blockingAwait();
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
                .doOnComplete(() -> reference.set(true))
                .blockingAwait();
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
                .doOnComplete(() -> reference.set(true))
                .blockingAwait();
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
            completable.blockingAwait();
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
            completable.blockingAwait();
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
                .doOnComplete(() -> reference.set(true))
                .blockingAwait();
        assertThat(reference).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingOneDelayedValue() {
        AtomicBoolean reference = new AtomicBoolean();
        Completable completable = converter.fromPublisher(Flowable.just("hello").delay(10, TimeUnit.MILLISECONDS));
        completable
                .doOnComplete(() -> reference.set(true))
                .blockingAwait();
        assertThat(reference).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BoomException.class)
    public void testFromPublisherEmittingAnImmediateFailure() {
        Completable completable = converter.fromPublisher(Flowable.error(new BoomException("BOOM")));
        completable.blockingAwait();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BoomException.class)
    public void testFromPublisherEmittingAnDelayedFailure() {
        Completable completable = converter.fromPublisher(Flowable.just("hello")
                .delay(10, TimeUnit.MILLISECONDS))
                .doOnComplete(() -> {
                    throw new BoomException("BOOM");
                });
        completable.blockingAwait();
    }

    @Test
    public void testFromEmptyPublisher() {
        AtomicBoolean reference = new AtomicBoolean();
        Completable completable = converter.fromPublisher(Flowable.empty());
        completable
                .doOnComplete(() -> reference.set(true))
                .blockingAwait();
        assertThat(reference).isTrue();

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingImmediateMultipleValue() {
        AtomicBoolean reference = new AtomicBoolean();
        Completable completable = converter.fromPublisher(Flowable.just("h", "e", "l", "l", "o"));
        completable
                .doOnComplete(() -> reference.set(true))
                .blockingAwait();
        assertThat(reference).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingDelayedMultipleValue() {
        AtomicBoolean reference = new AtomicBoolean();
        Completable completable = converter.fromPublisher(Flowable.just("h", "e", "l", "l", "o"))
                .observeOn(Schedulers.computation());
        completable
                .doOnComplete(() -> reference.set(true))
                .blockingAwait();
        assertThat(reference).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void testFromPublisherEmittingANullValueImmediately() {
        @SuppressWarnings("ConstantConditions") Completable completable = converter.fromPublisher(Flowable.just(null));
        completable.blockingAwait();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void testFromPublisherEmittingADelayedNullValue() {
        Completable completable = converter.fromPublisher(Flowable.just("hello").delay(10, TimeUnit.MILLISECONDS)
                .map(x -> null)
        );
        completable.blockingAwait();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherThatIsNeverEmitting() throws InterruptedException {
        Completable completable = converter.fromPublisher(Flowable.never());
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            //noinspection ResultOfMethodCallIgnored
            completable.blockingAwait();
            latch.countDown();
        }).start();
        assertThat(latch.await(10, TimeUnit.MILLISECONDS)).isFalse();
    }


}