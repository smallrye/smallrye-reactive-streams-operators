package io.smallrye.reactive.converters.tck;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import org.junit.Test;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public abstract class FromRSPublisherTCK<T> {

    protected abstract boolean supportNullValues();

    protected abstract boolean emitSingleValue();

    protected abstract boolean emitMultipleValues();

    protected abstract ReactiveTypeConverter<T> converter();

    protected abstract String getOne(T instance);

    protected abstract Exception getFailure(T instance);

    protected abstract List<String> getAll(T instance);

    protected abstract void consume(T instance);

    @Test
    public void testWithImmediateValue() {
        String uuid = UUID.randomUUID().toString();
        Publisher<String> publisher = Flowable.just(uuid);
        T instance = converter()
                .fromPublisher(publisher);
        String res = getOne(instance);
        if (emitSingleValue()  || emitMultipleValues()) {
            assertThat(res).isEqualTo(uuid);
        } else {
            assertThat(res).isNull();
        }
    }

    @Test
    public void testWithAsynchronousValue() {
        String uuid = UUID.randomUUID().toString();
        Publisher<String> publisher = Flowable.just(uuid)
                .delay(10, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.computation());
        T instance = converter()
                .fromPublisher(publisher);
        String res = getOne(instance);
        if (emitSingleValue() || emitMultipleValues()) {
            assertThat(res).isEqualTo(uuid);
        } else {
            assertThat(res).isNull();
        }
    }

    @Test
    public void testWithImmediateFailure() {
        Publisher<String> publisher = Flowable.error(new BoomException());
        T instance = converter()
                .fromPublisher(publisher);

        Exception e = getFailure(instance);
        assertThat(e).isNotNull()
                .isInstanceOf(BoomException.class);
    }

    @Test
    public void testWithAsynchronousFailure() {
        Publisher<String> publisher = Flowable.just("X")
                .delay(10, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.computation())
                .map(s -> {
                    throw new BoomException();
                });
        T instance = converter()
                .fromPublisher(publisher);

        Exception e = getFailure(instance);
        assertThat(e).isNotNull()
                .isInstanceOf(BoomException.class);
    }

    @Test
    public void testWithImmediateNullValue() {
        Publisher<String> publisher = Flowable.just("X").map(s -> null);
        assertNullPointerExceptionWhenNullIsEmitted(publisher);
    }

    @Test
    public void testWithAsynchronousNullValue() {
        Publisher<String> publisher = Flowable.just("X")
                .delay(10, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.computation())
                .map(s -> null);
        assertNullPointerExceptionWhenNullIsEmitted(publisher);
    }

    private void assertNullPointerExceptionWhenNullIsEmitted(Publisher<String> publisher) {
        try {
            T instance = converter().fromPublisher(publisher);
            getOne(instance);
            fail("Exception expected when publishing `null`");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    public void testWithNever() throws InterruptedException {
        Publisher<String> never = Flowable.never();
        T instance = converter().fromPublisher(never);
        CountDownLatch latch = new CountDownLatch(1);
        Future<?> future = Executors.newSingleThreadExecutor().submit(() -> {
            getOne(instance);
            latch.countDown();
        });
        boolean terminated = latch.await(10, TimeUnit.MILLISECONDS);
        future.cancel(true);
        assertThat(terminated).isFalse();
    }

    @Test
    public void testWithEmpty() {
        Publisher<String> empty = Flowable.empty();
        T instance = converter().fromPublisher(empty);
        if (! emitSingleValue()) {
            int count = getAll(instance).size();
            assertThat(count).isEqualTo(0);
        } else {
            try {
                getOne(instance);
            } catch (Exception e) {
                assertThat(e).isInstanceOf(NoSuchElementException.class);
            }
        }
    }

    @Test
    public void testWithMultipleValues() {
        Publisher<String> count = Flowable.range(0, 10).map(i -> Integer.toString(i));
        T instance = converter().fromPublisher(count);
        if (emitMultipleValues()) {
            List<String> list = getAll(instance);
            assertThat(list).containsExactly("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        } else if (emitSingleValue()) {
            String val = getOne(instance);
            assertThat(val).isEqualTo("0");
        } else {
            String x = getOne(instance);
            assertThat(x).isNull();
        }
    }

    @Test
    public void testWithMultipleValuesFollowedByAFailure() {
        Publisher<String> publisher = Flowable.just("a", "b", "c").map(s -> {
            if (s.equalsIgnoreCase("c")) {
                throw new BoomException();
            }
            return s;
        });
        T instance = converter().fromPublisher(publisher);
        if (emitMultipleValues()) {
            try {
                getAll(instance);
            } catch (Exception e) {
                assertThat(e).isInstanceOf(BoomException.class);
            }
        } else if (emitSingleValue()) {
            try {
                getOne(instance);
            } catch (Exception e) {
                assertThat(e).isInstanceOf(BoomException.class);
            }
        } else {
            try {
                consume(instance);
            } catch (Exception e) {
                assertThat(e).isInstanceOf(BoomException.class);
            }
        }
    }


}
