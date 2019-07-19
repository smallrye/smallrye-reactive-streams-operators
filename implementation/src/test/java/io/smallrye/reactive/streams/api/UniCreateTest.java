package io.smallrye.reactive.streams.api;

import io.smallrye.reactive.streams.api.impl.DefaultUniEmitter;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class UniCreateTest {

    @Test(expected = IllegalArgumentException.class)
    public void testThatConsumerCannotBeNull() {
         Uni.from().emitter(null);
    }

    @Test
    public void testThatOnlyTheFirstSignalIsConsidered() {
        AtomicReference<UniEmitter> reference = new AtomicReference<>();
        Uni<Integer> uni =  Uni.from().emitter(emitter -> {
            reference.set(emitter);
            emitter.success(1);
            emitter.fail(new Exception());
            emitter.success(2);
            emitter.success();
        });
        AssertSubscriber<Integer> subscriber = AssertSubscriber.create();
        uni.subscribe().withSubscriber(subscriber);

        subscriber.assertCompletedSuccessfully().assertResult(1);
        // Other signals are dropped
        assertThat(((DefaultUniEmitter) reference.get()).isDisposed()).isTrue();
    }

    @Test
    public void testWithOnCancellationAction() {
        AssertSubscriber<Integer> subscriber = AssertSubscriber.create();
        AtomicInteger onCancellationCalled = new AtomicInteger();
         Uni.from().<Integer>emitter(emitter -> {
            emitter.onCancellation(onCancellationCalled::incrementAndGet).success(1);
            emitter.fail(new Exception());
            emitter.success(2);
            emitter.success();
        }).subscribe().withSubscriber(subscriber);

        subscriber.assertResult(1);
        assertThat(onCancellationCalled).hasValue(1);
        subscriber.cancel();
        assertThat(onCancellationCalled).hasValue(1);
    }

    @Test
    public void testWithCancellation() {
        AssertSubscriber<Integer> subscriber = AssertSubscriber.create();
        AtomicInteger onCancellationCalled = new AtomicInteger();
         Uni.from().<Integer>emitter(emitter -> emitter.onCancellation(onCancellationCalled::incrementAndGet)).subscribe().withSubscriber(subscriber);

        assertThat(onCancellationCalled).hasValue(0);
        subscriber.cancel();
        assertThat(onCancellationCalled).hasValue(1);
    }

    @Test
    public void testWithFailure() {
        AssertSubscriber<Integer> subscriber = AssertSubscriber.create();
         Uni.from().<Integer>emitter(emitter -> emitter.fail(new Exception("boom"))).subscribe().withSubscriber(subscriber);

        subscriber.assertFailure(Exception.class, "boom");
    }

    @Test
    public void testWhenTheCallbackThrowsAnException() {
        AssertSubscriber<Integer> subscriber = AssertSubscriber.create();
         Uni.from().<Integer>emitter(emitter -> {
            throw new NullPointerException("boom");
        }).subscribe().withSubscriber(subscriber);

        subscriber.assertFailure(NullPointerException.class, "boom");

    }

    @Test
    public void testThatEmitterIsDisposed() {
        AssertSubscriber<Void> subscriber = AssertSubscriber.create();
        AtomicReference<UniEmitter<? super Void>> reference = new AtomicReference<>();
        Uni.from().<Void>emitter(emitter -> {
            reference.set(emitter);
            emitter.success();
        }).subscribe().withSubscriber(subscriber);

        subscriber.assertCompletedSuccessfully();
        assertThat(reference.get()).isInstanceOf(DefaultUniEmitter.class).satisfies(e -> ((DefaultUniEmitter) e).isDisposed());
    }

    @Test
    public void testThatFailuresCannotBeNull() {
        AssertSubscriber<Integer> subscriber = AssertSubscriber.create();
         Uni.from().<Integer>emitter(emitter -> emitter.fail(null)).subscribe().withSubscriber(subscriber);

        subscriber.assertFailure(IllegalArgumentException.class, "");
    }

    @Test
    public void testFailureThrownBySubscribersOnResult() {
        AtomicBoolean called = new AtomicBoolean();
        AtomicBoolean onCancellationCalled = new AtomicBoolean();
         Uni.from().<Integer>emitter(emitter -> {
            emitter.onCancellation(() -> onCancellationCalled.set(true));
            try {
                emitter.success(1);
                fail("Exception expected");
            } catch (Exception ex) {
                // expected
            }
        })
                .subscribe().withSubscriber(new UniSubscriber<Integer>() {

                    @Override
                    public void onSubscribe(UniSubscription subscription) {

                    }

                    @Override
                    public void onResult(Integer result) {
                        throw new NullPointerException("boom");
                    }

                    @Override
                    public void onFailure(Throwable failure) {
                        called.set(true);
                    }
                });

        assertThat(called).isFalse();
        assertThat(onCancellationCalled).isTrue();
    }

    @Test
    public void testFailureThrownBySubscribersOnFailure() {
        AtomicBoolean onCancellationCalled = new AtomicBoolean();
         Uni.from().<Integer>emitter(emitter -> {
            emitter.onCancellation(() -> onCancellationCalled.set(true));
            try {
                emitter.fail(new Exception("boom"));
                fail("Exception expected");
            } catch (Exception ex) {
                // expected
            }
        })
                .subscribe().withSubscriber(new UniSubscriber<Integer>() {

                    @Override
                    public void onSubscribe(UniSubscription subscription) {

                    }

                    @Override
                    public void onResult(Integer result) {
                    }

                    @Override
                    public void onFailure(Throwable failure) {
                        throw new NullPointerException("boom");
                    }
                });

        assertThat(onCancellationCalled).isTrue();
    }

}
