package io.smallrye.reactive.converters.rxjava2;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import io.smallrye.reactive.converters.Registry;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ObservableConverterTest {

    private ReactiveTypeConverter<Observable> converter;

    @Before
    public void lookup() {
        converter = Registry.lookup(Observable.class)
                .orElseThrow(() -> new AssertionError("Observable converter should be found"));
    }

    @Test
    public void testToCompletionStageWithImmediateValue() {
        Observable<String> observable = Observable.just("hello");
        CompletionStage<Optional<String>> stage = converter.toCompletionStage(observable);
        Optional<String> res = stage.toCompletableFuture().join();
        assertThat(res).contains("hello");
    }

    @Test
    public void testToCompletionStageWithDelayedValue() {
        Observable<String> observable = Observable.just("hello").delay(10, TimeUnit.MILLISECONDS);
        CompletionStage<Optional<String>> stage = converter.toCompletionStage(observable);
        Optional<String> res = stage.toCompletableFuture().join();
        assertThat(res).contains("hello");
    }

    @Test
    public void testToCompletionStageWithImmediateCompletion() {
        Observable<String> observable = Observable.empty();
        CompletionStage<Optional<String>> stage = converter.toCompletionStage(observable);
        Optional<String> res = stage.toCompletableFuture().join();
        assertThat(res).isEmpty();
    }

    @Test
    public void testToCompletionStageWithDelayedCompletion() {
        Observable<?> observable = Observable.empty().observeOn(Schedulers.computation());
        CompletionStage<Optional<String>> stage = converter.toCompletionStage(observable);
        Optional<String> res = stage.toCompletableFuture().join();
        assertThat(res).isEmpty();
    }

    @Test
    public void testToCompletionStageWithMultipleValuesEmittedImmediately() {
        Observable<String> observable = Observable.just("a", "b", "c");
        CompletionStage<Optional<String>> stage = converter.toCompletionStage(observable);
        Optional<String> res = stage.toCompletableFuture().join();
        assertThat(res).contains("a");
    }

    @Test
    public void testToCompletionStageWithMultipleValuesDelayed() {
        Observable<String> observable = Observable.just("a", "b", "c").delay(10, TimeUnit.MILLISECONDS);
        CompletionStage<Optional<String>> stage = converter.toCompletionStage(observable);
        Optional<String> res = stage.toCompletableFuture().join();
        assertThat(res).contains("a");
    }

    @Test
    public void testToCompletionStageWithImmediateFailure() {
        Observable<String> observable = Observable.error(new BoomException("BOOM"));
        CompletionStage<String> stage = converter.toCompletionStage(observable);
        try {
            stage.toCompletableFuture().join();
            fail("Exception expected");
        } catch (CompletionException e) {
            assertThat(e.getCause()).isInstanceOf(BoomException.class);
        }
    }

    @Test
    public void testToCompletionStageWithDelayedFailure() {
        Observable<String> observable = Observable.just("hello")
                .delay(10, TimeUnit.MILLISECONDS)
                .map(x -> {
                    throw new BoomException("BOOM");
                });
        CompletionStage<String> stage = converter.toCompletionStage(observable);
        try {
            stage.toCompletableFuture().join();
            fail("Exception expected");
        } catch (CompletionException e) {
            assertThat(e.getCause()).isInstanceOf(BoomException.class);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testToCompletionStageWithNullValue() {
        @SuppressWarnings("ConstantConditions") Observable<String> observable = Observable.just(null);
        CompletionStage<String> stage = converter.toCompletionStage(observable);
        stage.toCompletableFuture().join();
    }

    @Test
    public void testToCompletionStageWithDelayedNullValue() {
        Observable<String> observable = Observable.just("goo").delay(10, TimeUnit.MILLISECONDS).map(s -> null);
        CompletionStage<String> stage = converter.toCompletionStage(observable);
        try {
            stage.toCompletableFuture().join();
            fail("Exception expected");
        } catch (CompletionException e) {
            assertThat(e).hasCauseInstanceOf(NullPointerException.class);
        }
    }

    @Test
    public void testToCompletionStageWithStreamNotEmitting() {
        Observable<String> observable = Observable.never();
        CompletionStage<String> stage = converter.toCompletionStage(observable);
        try {
            stage.toCompletableFuture().get(10, TimeUnit.MILLISECONDS);
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(TimeoutException.class);
        }
    }

    @Test
    public void testToPublisherWithImmediateValue() {
        Observable<String> stream = Observable.just("hello");
        Observable<String> flowable = Observable.fromPublisher(converter.toRSPublisher(stream));
        String res = flowable.blockingFirst();
        assertThat(res).isEqualTo("hello");
    }

    @Test
    public void testToPublisherWithDelayedValue() {
        Observable<String> stream = Observable.just("hello").delay(10, TimeUnit.MILLISECONDS);
        Observable<String> flowable = Observable.fromPublisher(converter.toRSPublisher(stream));
        String res = flowable.blockingFirst();
        assertThat(res).isEqualTo("hello");
    }

    @Test
    public void testToPublisherWithImmediateValues() {
        Observable<String> stream = Observable.just("h", "e", "l", "l", "o");
        Observable<String> flowable = Observable.fromPublisher(converter.toRSPublisher(stream));
        List<String> res = flowable.toList().blockingGet();
        assertThat(res).containsExactly("h", "e", "l", "l", "o");
    }

    @Test
    public void testToPublisherWithDelayedValues() {
        Observable<String> stream = Observable.just("h", "e", "l", "l", "o").delay(10, TimeUnit.MILLISECONDS);
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(stream));
        List<String> res = flowable.toList().blockingGet();
        assertThat(res).containsExactly("h", "e", "l", "l", "o");
    }

    @Test
    public void testToPublisherWithImmediateFailure() {
        Observable<String> stream = Observable.error(new BoomException("BOOM"));
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(stream));
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
        Observable<String> stream = Observable.just("hello")
                .delay(10, TimeUnit.MILLISECONDS)
                .map(x -> {
                    throw new BoomException("BOOM");
                });
        Observable<String> flowable = Observable.fromPublisher(converter.toRSPublisher(stream));
        try {
            //noinspection ResultOfMethodCallIgnored
            flowable.blockingFirst();
            fail("Exception expected");
        } catch (BoomException e) {
            assertThat(e).hasMessage("BOOM");
        }
    }

    @Test
    public void testToPublisherWithItemAndFailure() {
        Observable<String> stream = Observable.just("a", "b", "c")
                .map(s -> {
                    if (s.equalsIgnoreCase("c")) {
                        throw new BoomException("BOOM");
                    }
                    return s;
                });
        Observable<String> flowable = Observable.fromPublisher(converter.toRSPublisher(stream));
        List<String> items = new CopyOnWriteArrayList<>();
        try {
            //noinspection ResultOfMethodCallIgnored
            flowable
                    .doOnNext(items::add)
                    .toList()
                    .blockingGet();
            fail("Exception expected");
        } catch (BoomException e) {
            assertThat(e).hasMessage("BOOM");
        }
        assertThat(items).containsExactly("a", "b");
    }

    @Test
    public void testToPublisherWithDelayedItemAndFailure() {
        Observable<String> stream = Observable.just("a", "b", "c")
                .delay(10, TimeUnit.MILLISECONDS)
                .map(s -> {
                    if (s.equalsIgnoreCase("c")) {
                        throw new BoomException("BOOM");
                    }
                    return s;
                });
        Observable<String> flowable = Observable.fromPublisher(converter.toRSPublisher(stream));
        List<String> items = new CopyOnWriteArrayList<>();
        try {
            //noinspection ResultOfMethodCallIgnored
            flowable
                    .doOnNext(items::add)
                    .toList()
                    .blockingGet();
            fail("Exception expected");
        } catch (BoomException e) {
            assertThat(e).hasMessage("BOOM");
        }
        assertThat(items).containsExactly("a", "b");
    }

    @Test(expected = NullPointerException.class)
    public void testToPublisherWithNullValue() {
        @SuppressWarnings("ConstantConditions") Observable<String> stream = Observable.just(null);
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(stream));
        //noinspection ResultOfMethodCallIgnored
        flowable.blockingFirst();
    }

    @Test(expected = NullPointerException.class)
    public void testToPublisherWithDelayedNullValue() {
        Observable<String> stream = Observable.just("goo").delay(10, TimeUnit.MILLISECONDS).map(s -> null);
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(stream));
        //noinspection ResultOfMethodCallIgnored
        flowable.blockingFirst();
    }

    @Test
    public void testToPublisherWithStreamNotEmitting() throws InterruptedException {
        Observable<String> stream = Observable.never();
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(stream));
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
        AtomicBoolean done = new AtomicBoolean();
        @SuppressWarnings("unchecked")
        Observable<String> observable = converter
                .fromCompletionStage(CompletableFuture.completedFuture("hello"))
                .cast(String.class);
        assertThat(observable
                .doOnNext(reference::set)
                .doOnComplete(() -> done.set(true))
                .blockingFirst())
                .isEqualTo("hello");
        assertThat(reference).hasValue("hello");
        assertThat(done).isTrue();
    }

    @Test
    public void testFromCompletionStageWithDelayedValue() {
        AtomicReference<String> reference = new AtomicReference<>();
        AtomicBoolean done = new AtomicBoolean();
        @SuppressWarnings("unchecked")
        Observable<String> observable = converter
                .fromCompletionStage(CompletableFuture.supplyAsync(() -> "hello"))
                .cast(String.class);
        assertThat(observable
                .doOnComplete(() -> done.set(true))
                .doOnNext(reference::set)
                .blockingFirst()).isEqualTo("hello");
        assertThat(reference).hasValue("hello");
        assertThat(done).isTrue();
    }

    @Test
    public void testFromCompletionStageWithImmediateFailure() {
        AtomicReference<Throwable> reference = new AtomicReference<>();
        CompletableFuture<String> future = new CompletableFuture<>();
        @SuppressWarnings("unchecked")
        Observable<String> observable = converter
                .fromCompletionStage(future)
                .cast(String.class);
        future.completeExceptionally(new BoomException("BOOM"));
        try {
            //noinspection ResultOfMethodCallIgnored
            observable
                    .doOnError(reference::set)
                    .blockingFirst();
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
        Observable<String> observable = converter
                .fromCompletionStage(future)
                .cast(String.class);
        try {
            //noinspection ResultOfMethodCallIgnored
            observable
                    .doOnError(reference::set)
                    .blockingFirst();
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
        Observable<String> observable = converter
                .fromCompletionStage(future);
        //noinspection ResultOfMethodCallIgnored
        String val = observable.blockingFirst("DEFAULT");
        assertThat(val).isEqualTo("DEFAULT");
    }

    @Test
    public void testFromCompletionStageWithDelayedNullValue() {
        CompletionStage<Void> future = CompletableFuture.supplyAsync(() -> null);
        @SuppressWarnings("unchecked")
        Observable<String> observable = converter
                .fromCompletionStage(future);
        //noinspection ResultOfMethodCallIgnored
        String val = observable.blockingFirst("DEFAULT");
        assertThat(val).isEqualTo("DEFAULT");
    }

    @Test
    public void testFromCompletionStageThatGetCancelled() {
        CompletableFuture<String> future = new CompletableFuture<>();
        @SuppressWarnings("unchecked")
        Observable<String> observable = converter
                .fromCompletionStage(future)
                .cast(String.class);
        future.cancel(false);
        try {
            //noinspection ResultOfMethodCallIgnored
            observable.blockingFirst();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(CancellationException.class);
        }
    }

    @Test
    public void testFromCompletionStageWithoutRedeemingAValue() throws InterruptedException {
        CompletionStage<String> never = new CompletableFuture<>();
        @SuppressWarnings("unchecked") Observable<String> flowable = converter.fromCompletionStage(never);
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            //noinspection ResultOfMethodCallIgnored
            flowable.blockingFirst();
            latch.countDown();
        }).start();
        assertThat(latch.await(10, TimeUnit.MILLISECONDS)).isFalse();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingOneImmediateValue() {
        Observable<?> observable = converter.fromPublisher(ReactiveStreams.of("hello").buildRs());
        String o = observable
                .cast(String.class)
                .blockingFirst();
        assertThat(o).isEqualTo("hello");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingOneDelayedValue() {
        Observable<?> observable = converter.fromPublisher(Flowable.just("hello").delay(10, TimeUnit.MILLISECONDS));
        String o = observable
                .cast(String.class)
                .blockingFirst();
        assertThat(o).isEqualTo("hello");
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BoomException.class)
    public void testFromPublisherEmittingAnImmediateFailure() {
        Observable<?> observable = converter.fromPublisher(Flowable.error(new BoomException("BOOM")));
        //noinspection ResultOfMethodCallIgnored
        observable.cast(String.class).blockingFirst();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BoomException.class)
    public void testFromPublisherEmittingAnDelayedFailure() {
        Observable<?> observable = converter.fromPublisher(Flowable.just("hello")
                .delay(10, TimeUnit.MILLISECONDS))
                .map(x -> {
                    throw new BoomException("BOOM");
                });
        //noinspection ResultOfMethodCallIgnored
        observable.cast(String.class).blockingFirst();
    }

    @Test(expected = NoSuchElementException.class)
    public void testFromEmptyPublisher() {
        Observable<?> observable = converter.fromPublisher(Flowable.empty());
        //noinspection ResultOfMethodCallIgnored
        observable.blockingFirst();
        fail("Exception expected");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingImmediateMultipleValue() {
        Observable<?> flowable = converter.fromPublisher(Flowable.just("h", "e", "l", "l", "o"));
        List<String> o = flowable
                .cast(String.class)
                .toList()
                .blockingGet();
        assertThat(o).containsExactly("h", "e", "l", "l", "o");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingDelayedMultipleValue() {
        Observable<?> observable = converter.fromPublisher(Flowable.just("h", "e", "l", "l", "o")
                .observeOn(Schedulers.computation())
        );
        List<String> o = observable
                .cast(String.class)
                .toList()
                .blockingGet();
        assertThat(o).containsExactly("h", "e", "l", "l", "o");
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void testFromPublisherEmittingANullValueImmediately() {
        @SuppressWarnings("ConstantConditions") Observable<?> observable = converter.fromPublisher(Flowable.just(null));
        //noinspection ResultOfMethodCallIgnored
        observable.cast(String.class).blockingFirst();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void testFromPublisherEmittingADelayedNullValue() {
        Observable<?> observable = converter.fromPublisher(Flowable.just("hello").delay(10, TimeUnit.MILLISECONDS)
                .map(x -> null)
        );
        //noinspection ResultOfMethodCallIgnored
        observable.cast(String.class).blockingFirst();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherThatIsNeverEmitting() throws InterruptedException {
        Observable<?> observable = converter.fromPublisher(Flowable.never());

        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            //noinspection ResultOfMethodCallIgnored
            observable.blockingFirst();
            latch.countDown();
        }).start();
        assertThat(latch.await(10, TimeUnit.MILLISECONDS)).isFalse();
    }

    


}