package io.smallrye.reactive.converters.rxjava1;


import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Flowable;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import io.smallrye.reactive.converters.Registry;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
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
    public void testToPublisherWithImmediateValue() {
        Observable<String> stream = Observable.just("hello");
        Observable<String> flowable = RxJavaInterop.toV1Observable(converter.toRSPublisher(stream));
        String res = flowable.toBlocking().first();
        assertThat(res).isEqualTo("hello");
    }

    @Test
    public void testToPublisherWithDelayedValue() {
        Observable<String> stream = Observable.just("hello").delay(10, TimeUnit.MILLISECONDS);
        Observable<String> flowable = RxJavaInterop.toV1Observable(converter.toRSPublisher(stream));
        String res = flowable.toBlocking().first();
        assertThat(res).isEqualTo("hello");
    }

    @Test
    public void testToPublisherWithImmediateValues() {
        Observable<String> stream = Observable.just("h", "e", "l", "l", "o");
        Observable<String> flowable = RxJavaInterop.toV1Observable(converter.toRSPublisher(stream));
        Iterator<String> res = flowable.toBlocking().getIterator();
        assertThat(res).containsExactly("h", "e", "l", "l", "o");
    }

    @Test
    public void testToPublisherWithDelayedValues() {
        Observable<String> stream = Observable.just("h", "e", "l", "l", "o").delay(10, TimeUnit.MILLISECONDS);
        Observable<String> flowable = RxJavaInterop.toV1Observable(converter.toRSPublisher(stream));
        Iterator<String> res = flowable.toBlocking().getIterator();
        assertThat(res).containsExactly("h", "e", "l", "l", "o");
    }

    @Test
    public void testToPublisherWithImmediateFailure() {
        Observable<String> stream = Observable.error(new BoomException("BOOM"));
        Observable<String> observable = RxJavaInterop.toV1Observable(converter.toRSPublisher(stream));
        try {
            //noinspection ResultOfMethodCallIgnored
            observable.toBlocking().first();
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
        Observable<String> observable = RxJavaInterop.toV1Observable(converter.toRSPublisher(stream));
        try {
            //noinspection ResultOfMethodCallIgnored
            observable.toBlocking().first();
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
        Observable<String> observable = RxJavaInterop.toV1Observable(converter.toRSPublisher(stream));
        List<String> items = new CopyOnWriteArrayList<>();
        try {
            //noinspection ResultOfMethodCallIgnored
            observable
                    .doOnNext(items::add)
                    .toList()
                    .toBlocking()
                    .first();
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
        Observable<String> observable = RxJavaInterop.toV1Observable(converter.toRSPublisher(stream));
        List<String> items = new CopyOnWriteArrayList<>();
        try {
            //noinspection ResultOfMethodCallIgnored
            observable
                    .doOnNext(items::add)
                    .toList()
                    .toBlocking()
                    .first();
            fail("Exception expected");
        } catch (BoomException e) {
            assertThat(e).hasMessage("BOOM");
        }
        assertThat(items).containsExactly("a", "b");
    }

    @Test(expected = NullPointerException.class)
    public void testToPublisherWithNullValue() {
        @SuppressWarnings("ConstantConditions") Observable<String> stream = Observable.just(null);
        Observable<String> observable = RxJavaInterop.toV1Observable(converter.toRSPublisher(stream));
        //noinspection ResultOfMethodCallIgnored
        observable.toBlocking().first();
    }

    @Test
    public void testToPublisherWithNullValueAfterAFewItems() {
        @SuppressWarnings("ConstantConditions") Observable<String> stream = Observable.just("a", "b", null, "c");
        Flowable<String> flow = Flowable.fromPublisher(converter.toRSPublisher(stream));
        List<String> list = new ArrayList<>();
        AtomicReference<Throwable> err = new AtomicReference<>();
        try {
            flow
                    .doOnNext(list::add)
                    .doOnError(err::set)
                    .blockingSubscribe();
            fail("Expected an NPE");
        } catch (NullPointerException e) {
            assertThat(list).containsExactly("a", "b");
            assertThat(err.get()).isInstanceOf(NullPointerException.class);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testToPublisherWithDelayedNullValue() {
        Observable<String> stream = Observable.just("goo").delay(10, TimeUnit.MILLISECONDS).map(s -> null);
        Observable<String> observable = RxJavaInterop.toV1Observable(converter.toRSPublisher(stream));
        //noinspection ResultOfMethodCallIgnored
        observable.toBlocking().first();
    }

    @Test
    public void testToPublisherWithStreamNotEmitting() throws InterruptedException {
        Observable<String> stream = Observable.never();
        Observable<String> observable = RxJavaInterop.toV1Observable(converter.toRSPublisher(stream));
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            //noinspection ResultOfMethodCallIgnored
            observable.toBlocking().first();
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
                .doOnCompleted(() -> done.set(true))
                .toBlocking()
                .last())
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
                .doOnCompleted(() -> done.set(true))
                .doOnNext(reference::set)
                .toBlocking().last())
                .isEqualTo("hello");
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
                    .toBlocking().first();
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
                    .toBlocking().first();
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
        String val = observable.toBlocking().first();
        assertThat(val).isNull();
    }

    @Test
    public void testFromCompletionStageWithDelayedNullValue() {
        CompletionStage<Void> future = CompletableFuture.supplyAsync(() -> null);
        @SuppressWarnings("unchecked")
        Observable<String> observable = converter
                .fromCompletionStage(future);
        String val = observable.toBlocking().first();
        assertThat(val).isNull();
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
            observable.toBlocking().first();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(CancellationException.class);
        }
    }

    @Test
    public void testFromCompletionStageWithoutRedeemingAValue() throws InterruptedException {
        CompletionStage<String> never = new CompletableFuture<>();
        @SuppressWarnings("unchecked") Observable<String> observable = converter.fromCompletionStage(never);
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            //noinspection ResultOfMethodCallIgnored
            observable.toBlocking().first();
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
                .toBlocking().first();
        assertThat(o).isEqualTo("hello");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingOneDelayedValue() {
        Observable<?> observable = converter.fromPublisher(Flowable.just("hello").delay(10, TimeUnit.MILLISECONDS));
        String o = observable
                .cast(String.class)
                .toBlocking().first();
        assertThat(o).isEqualTo("hello");
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BoomException.class)
    public void testFromPublisherEmittingAnImmediateFailure() {
        Observable<?> observable = converter.fromPublisher(Flowable.error(new BoomException("BOOM")));
        //noinspection ResultOfMethodCallIgnored
        observable.cast(String.class).toBlocking().first();
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
        observable.cast(String.class).toBlocking().first();
    }

    @Test(expected = NoSuchElementException.class)
    public void testFromEmptyPublisher() {
        Observable<?> observable = converter.fromPublisher(Flowable.empty());
        //noinspection ResultOfMethodCallIgnored
        observable.toBlocking().first();
        fail("Exception expected");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingImmediateMultipleValue() {
        Observable<?> flowable = converter.fromPublisher(Flowable.just("h", "e", "l", "l", "o"));
        List<String> o = flowable
                .cast(String.class)
                .toList()
                .toBlocking().first();
        assertThat(o).containsExactly("h", "e", "l", "l", "o");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingDelayedMultipleValue() {
        Observable<?> observable = converter.fromPublisher(Flowable.just("h", "e", "l", "l", "o")
                .observeOn(io.reactivex.schedulers.Schedulers.computation())
        );
        List<String> o = observable
                .cast(String.class)
                .toList()
                .toBlocking().first();
        assertThat(o).containsExactly("h", "e", "l", "l", "o");
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void testFromPublisherEmittingANullValueImmediately() {
        @SuppressWarnings("ConstantConditions") Observable<?> observable = converter.fromPublisher(Flowable.just(null));
        //noinspection ResultOfMethodCallIgnored
        observable.cast(String.class).toBlocking().first();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void testFromPublisherEmittingADelayedNullValue() {
        Observable<?> observable = converter.fromPublisher(Flowable.just("hello").delay(10, TimeUnit.MILLISECONDS)
                .map(x -> null)
        );
        //noinspection ResultOfMethodCallIgnored
        observable.cast(String.class).toBlocking().first();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherThatIsNeverEmitting() throws InterruptedException {
        Observable<?> observable = converter.fromPublisher(Flowable.never());

        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            //noinspection ResultOfMethodCallIgnored
            observable.toBlocking().first();
            latch.countDown();
        }).start();
        assertThat(latch.await(10, TimeUnit.MILLISECONDS)).isFalse();
    }
}