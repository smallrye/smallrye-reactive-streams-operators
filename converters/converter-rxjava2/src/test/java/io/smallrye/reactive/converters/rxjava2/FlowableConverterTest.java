package io.smallrye.reactive.converters.rxjava2;

import io.reactivex.Flowable;
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

public class FlowableConverterTest {

    private ReactiveTypeConverter<Flowable> converter;

    @Before
    public void lookup() {
        converter = Registry.lookup(Flowable.class)
                .orElseThrow(() -> new AssertionError("Flowable converter should be found"));
    }

    @Test
    public void testToPublisherWithImmediateValue() {
        Flowable<String> stream = Flowable.just("hello");
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(stream));
        String res = flowable.blockingFirst();
        assertThat(res).isEqualTo("hello");
    }

    @Test
    public void testToPublisherWithDelayedValue() {
        Flowable<String> stream = Flowable.just("hello").delay(10, TimeUnit.MILLISECONDS);
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(stream));
        String res = flowable.blockingFirst();
        assertThat(res).isEqualTo("hello");
    }

    @Test
    public void testToPublisherWithImmediateValues() {
        Flowable<String> stream = Flowable.just("h", "e", "l", "l", "o");
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(stream));
        List<String> res = flowable.toList().blockingGet();
        assertThat(res).containsExactly("h", "e", "l", "l", "o");
    }

    @Test
    public void testToPublisherWithDelayedValues() {
        Flowable<String> stream = Flowable.just("h", "e", "l", "l", "o").delay(10, TimeUnit.MILLISECONDS);
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(stream));
        List<String> res = flowable.toList().blockingGet();
        assertThat(res).containsExactly("h", "e", "l", "l", "o");
    }

    @Test
    public void testToPublisherWithImmediateFailure() {
        Flowable<String> stream = Flowable.error(new BoomException("BOOM"));
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
        Flowable<String> stream = Flowable.just("hello")
                .delay(10, TimeUnit.MILLISECONDS)
                .map(x -> {
                    throw new BoomException("BOOM");
                });
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
    public void testToPublisherWithItemAndFailure() {
        Flowable<String> stream = Flowable.just("a", "b", "c")
                .map(s -> {
                    if (s.equalsIgnoreCase("c")) {
                        throw new BoomException("BOOM");
                    }
                    return s;
                });
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(stream));
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
        Flowable<String> stream = Flowable.just("a", "b", "c")
                .delay(10, TimeUnit.MILLISECONDS)
                .map(s -> {
                    if (s.equalsIgnoreCase("c")) {
                        throw new BoomException("BOOM");
                    }
                    return s;
                });
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(stream));
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
        @SuppressWarnings("ConstantConditions") Flowable<String> stream = Flowable.just(null);
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(stream));
        //noinspection ResultOfMethodCallIgnored
        flowable.blockingFirst();
    }

    @Test(expected = NullPointerException.class)
    public void testToPublisherWithDelayedNullValue() {
        Flowable<String> stream = Flowable.just("goo").delay(10, TimeUnit.MILLISECONDS).map(s -> null);
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(stream));
        //noinspection ResultOfMethodCallIgnored
        flowable.blockingFirst();
    }

    @Test
    public void testToPublisherWithStreamNotEmitting() throws InterruptedException {
        Flowable<String> stream = Flowable.never();
        Flowable<String> flowable = Flowable.fromPublisher(converter.toRSPublisher(stream));
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
        Flowable<?> flowable = converter.fromPublisher(ReactiveStreams.of("hello").buildRs());
        String o = flowable
                .cast(String.class)
                .blockingFirst();
        assertThat(o).isEqualTo("hello");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingOneDelayedValue() {
        Flowable<?> flowable = converter.fromPublisher(Flowable.just("hello").delay(10, TimeUnit.MILLISECONDS));
        String o = flowable
                .cast(String.class)
                .blockingFirst();
        assertThat(o).isEqualTo("hello");
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BoomException.class)
    public void testFromPublisherEmittingAnImmediateFailure() {
        Flowable<?> flowable = converter.fromPublisher(Flowable.error(new BoomException("BOOM")));
        //noinspection ResultOfMethodCallIgnored
        flowable.cast(String.class).blockingFirst();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BoomException.class)
    public void testFromPublisherEmittingAnDelayedFailure() {
        Flowable<?> flowable = converter.fromPublisher(Flowable.just("hello")
                .delay(10, TimeUnit.MILLISECONDS))
                .map(x -> {
                    throw new BoomException("BOOM");
                });
        //noinspection ResultOfMethodCallIgnored
        flowable.cast(String.class).blockingFirst();
    }

    @Test(expected = NoSuchElementException.class)
    public void testFromEmptyPublisher() {
        Flowable<?> flowable = converter.fromPublisher(Flowable.empty());
        //noinspection ResultOfMethodCallIgnored
        flowable.blockingFirst();
        fail("Exception expected");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingImmediateMultipleValue() {
        Flowable<?> flowable = converter.fromPublisher(Flowable.just("h", "e", "l", "l", "o"));
        List<String> o = flowable
                .cast(String.class)
                .toList()
                .blockingGet();
        assertThat(o).containsExactly("h", "e", "l", "l", "o");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherEmittingDelayedMultipleValue() {
        Flowable<?> flowable = converter.fromPublisher(Flowable.just("h", "e", "l", "l", "o")
                .observeOn(Schedulers.computation())
        );
        List<String> o = flowable
                .cast(String.class)
                .toList()
                .blockingGet();
        assertThat(o).containsExactly("h", "e", "l", "l", "o");
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void testFromPublisherEmittingANullValueImmediately() {
        @SuppressWarnings("ConstantConditions") Flowable<?> flowable = converter.fromPublisher(Flowable.just(null));
        //noinspection ResultOfMethodCallIgnored
        flowable.cast(String.class).blockingFirst();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void testFromPublisherEmittingADelayedNullValue() {
        Flowable<?> flowable = converter.fromPublisher(Flowable.just("hello").delay(10, TimeUnit.MILLISECONDS)
                .map(x -> null)
        );
        //noinspection ResultOfMethodCallIgnored
        flowable.cast(String.class).blockingFirst();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromPublisherThatIsNeverEmitting() throws InterruptedException {
        Flowable<?> flowable = converter.fromPublisher(Flowable.never());

        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            //noinspection ResultOfMethodCallIgnored
            flowable.blockingFirst();
            latch.countDown();
        }).start();
        assertThat(latch.await(10, TimeUnit.MILLISECONDS)).isFalse();
    }




}