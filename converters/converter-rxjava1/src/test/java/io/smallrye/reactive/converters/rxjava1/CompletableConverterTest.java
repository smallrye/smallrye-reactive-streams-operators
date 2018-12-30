package io.smallrye.reactive.converters.rxjava1;

import io.reactivex.Flowable;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import io.smallrye.reactive.converters.Registry;
import org.junit.Before;
import org.junit.Test;
import rx.Completable;
import rx.Observable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CompletableConverterTest {

    private ReactiveTypeConverter<Completable> converter;

    @Before
    public void lookup() {
        converter = Registry.lookup(Completable.class)
                .orElseThrow(() -> new AssertionError("Completable converter should be found"));
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