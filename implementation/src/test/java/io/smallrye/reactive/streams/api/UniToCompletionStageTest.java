package io.smallrye.reactive.streams.api;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

public class UniToCompletionStageTest {

    private ExecutorService executor;

    @After
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    @Test
    public void testWithImmediateValue() {
        CompletableFuture<Integer> future = Uni.of(1).subscribe().asCompletionStage();
        assertThat(future).isNotNull();
        assertThat(future).isCompletedWithValue(1);
    }

    @Test
    public void testWithImmediateNullValue() {
        // TODO Should we have an empty or null method?
        CompletableFuture<Void> future = Uni.<Void>of(null).subscribe().asCompletionStage();
        assertThat(future).isNotNull();
        assertThat(future).isCompletedWithValue(null);
    }

    @Test
    public void testWithImmediateFailure() {
        CompletableFuture<Integer> future = Uni.<Integer>failed(new IOException("boom")).subscribe().asCompletionStage();
        assertThat(future).isNotNull();
        try {
            future.join();
            fail("exception expected");
        } catch (Exception e) {
            assertThat(e).hasCauseInstanceOf(IOException.class).hasMessageContaining("boom");
        }

    }


    @Test
    public void testThatSubscriptionsAreNotShared() {
        AtomicInteger count = new AtomicInteger(1);
        Uni<Integer> deferred = Uni.defer(() -> Uni.of(count.getAndIncrement()));
        CompletionStage<Integer> cs1 = deferred.subscribe().asCompletionStage();
        CompletionStage<Integer> cs2 = deferred.subscribe().asCompletionStage();
        assertThat(cs1).isNotNull();
        assertThat(cs2).isNotNull();

        assertThat(cs1).isCompletedWithValue(1);
        assertThat(cs2).isCompletedWithValue(2);
    }

    @Test
    public void testThatTwoSubscribersWithCache() {
        AtomicInteger count = new AtomicInteger(1);
        Uni<Integer> cached = Uni.defer(() -> Uni.of(count.getAndIncrement())).cache();
        CompletionStage<Integer> cs1 = cached.subscribe().asCompletionStage();
        CompletionStage<Integer> cs2 = cached.subscribe().asCompletionStage();
        assertThat(cs1).isNotNull();
        assertThat(cs2).isNotNull();
        assertThat(cs1).isCompletedWithValue(1);
        assertThat(cs1).isCompletedWithValue(1);
    }

    @Test
    public void testCancellationWithImmediateValue() {
        AtomicInteger value = new AtomicInteger(-1);
        CompletableFuture<Integer> future = Uni.of(1).subscribe().asCompletionStage()
                .whenComplete((res, fail) -> value.set(res));
        future.cancel(false);
        assertThat(future).isNotCancelled(); // Too late.
        assertThat(value).hasValue(1);
    }

    @Test
    public void testCancellationWithAsyncValue() {
        // TODO This is a very broken implementation of "delay" - to be replace once delay is implemented
        executor = Executors.newSingleThreadExecutor();
        AtomicInteger value = new AtomicInteger(-1);
        CompletableFuture<Integer> future = Uni.of(1).publishOn(executor).map(x -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return x;
        }).subscribe().asCompletionStage()
                .whenComplete((res, fail) -> value.set(res));

        future.cancel(false);
        assertThat(value).hasValue(-1);
    }


    @Test
    public void testWithAsyncValue() {
        executor = Executors.newSingleThreadScheduledExecutor();
        CompletableFuture<Integer> future = Uni.of(1).publishOn(executor).subscribe().asCompletionStage();
        await().until(future::isDone);
        assertThat(future).isCompletedWithValue(1);
    }

    @Test
    public void testWithAsyncNullValue() {
        executor = Executors.newSingleThreadScheduledExecutor();
        CompletableFuture<Void> future = Uni.<Void>of(null).publishOn(executor).subscribe().asCompletionStage();
        await().until(future::isDone);
        assertThat(future).isCompletedWithValue(null);
    }

    @Test
    public void testWithAsyncFailure() {
        executor = Executors.newSingleThreadScheduledExecutor();
        CompletableFuture<Integer> future = Uni.<Integer>failed(new IOException("boom")).publishOn(executor).subscribe().asCompletionStage();
        await().until(future::isDone);
        assertThat(future).isCompletedExceptionally();
    }

}
