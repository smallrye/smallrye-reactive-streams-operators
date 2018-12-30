package io.smallrye.reactive.converters.rxjava2;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import io.smallrye.reactive.converters.Registry;
import org.junit.Before;
import org.junit.Test;

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