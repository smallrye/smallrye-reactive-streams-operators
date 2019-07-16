package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class UniFlatMapTest {

    @Test
    public void testFlatMapWithImmediateValue() {
        AssertSubscriber<Integer> test = AssertSubscriber.create();
        Uni.of(1).flatMap(v -> Uni.of(2)).subscribe().withSubscriber(test);
        test.assertCompletedSuccessfully().assertResult(2).assertNoFailure();
    }

    @Test
    public void testWithImmediateCancellation() {
        AssertSubscriber<Integer> test = new AssertSubscriber<>(true);
        AtomicBoolean called = new AtomicBoolean();
        Uni.of(1).flatMap(v -> {
            called.set(true);
            return Uni.of(2);
        }).subscribe().withSubscriber(test);
        test.assertNotCompleted();
        assertThat(called).isFalse();
    }

    @Test
    public void testWithADeferredUi() {
        AssertSubscriber<Integer> test1 = AssertSubscriber.create();
        AssertSubscriber<Integer> test2 = AssertSubscriber.create();
        AtomicInteger count = new AtomicInteger(2);
        Uni<Integer> uni = Uni.of(1).flatMap(v -> Uni.defer(() -> Uni.of(count.incrementAndGet())));
        uni.subscribe().withSubscriber(test1);
        uni.subscribe().withSubscriber(test2);
        test1.assertCompletedSuccessfully().assertResult(3).assertNoFailure();
        test2.assertCompletedSuccessfully().assertResult(4).assertNoFailure();
    }

    @Test
    public void testWithAnUniResolvedAsynchronously() {
        AssertSubscriber<Integer> test = AssertSubscriber.create();
        Uni<Integer> uni = Uni.of(1).flatMap(v -> Uni.create(emitter -> new Thread(() -> emitter.success(42)).start()));
        uni.subscribe().withSubscriber(test);
        test.await().assertCompletedSuccessfully().assertResult(42).assertNoFailure();
    }

    @Test
    public void testWithAnUniResolvedAsynchronouslyWithAFailure() {
        AssertSubscriber<Integer> test = AssertSubscriber.create();
        Uni<Integer> uni = Uni.of(1).flatMap(v -> Uni.create(emitter -> new Thread(() -> emitter.fail(new IOException("boom"))).start()));
        uni.subscribe().withSubscriber(test);
        test.await().assertCompletedWithFailure().assertFailure(IOException.class, "boom");
    }

    @Test
    public void testThatMapperIsNotCalledOnUpstreamFailure() {
        AssertSubscriber<Integer> test = AssertSubscriber.create();
        AtomicBoolean called = new AtomicBoolean();
        Uni.failed(new Exception("boom")).flatMap(v -> {
            called.set(true);
            return Uni.of(2);
        }).subscribe().withSubscriber(test);
        test.await().assertCompletedWithFailure().assertFailure(Exception.class, "boom");
        assertThat(called).isFalse();
    }

    @Test
    public void testWithAMapperThrowingAnException() {
        AssertSubscriber<Integer> test = AssertSubscriber.create();
        AtomicBoolean called = new AtomicBoolean();
        Uni.of(1).<Integer>flatMap(v -> {
            called.set(true);
            throw new IllegalStateException("boom");
        }).subscribe().withSubscriber(test);
        test.await().assertCompletedWithFailure().assertFailure(IllegalStateException.class, "boom");
        assertThat(called).isTrue();
    }

    @Test
    public void testWithAMapperReturningNull() {
        AssertSubscriber<Integer> test = AssertSubscriber.create();
        AtomicBoolean called = new AtomicBoolean();
        Uni.of(1).<Integer>flatMap(v -> {
            called.set(true);
            return null;
        }).subscribe().withSubscriber(test);
        test.await().assertCompletedWithFailure().assertFailure(NullPointerException.class, "");
        assertThat(called).isTrue();
    }

    @Test(expected = NullPointerException.class)
    public void testThatTheMapperCannotBeNull() {
        Uni.of(1).flatMap(null);
    }

    @Test
    public void testWithCancellationBeforeEmission() {
        AssertSubscriber<Integer> test = AssertSubscriber.create();
        AtomicBoolean cancelled = new AtomicBoolean();
        @SuppressWarnings("unchecked")
        CompletableFuture<Integer> future = new CompletableFuture() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                cancelled.set(true);
                return true;
            }
        };

        Uni<Integer> uni = Uni.of(1).flatMap(v -> Uni.fromCompletionStage(future));
        uni.subscribe().withSubscriber(test);
        test.cancel();
        test.assertNotCompleted();
        assertThat(cancelled).isTrue();
    }
}