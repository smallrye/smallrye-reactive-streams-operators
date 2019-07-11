package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class FromCompletionStageUniOperatorTest {

    @Test
    public void testThatNullValueAreAccepted() {
        AssertSubscriber<String> ts = AssertSubscriber.create();
        CompletionStage<String> cs = new CompletableFuture<>();
        Uni.fromCompletionStage(cs).subscribe(ts);
        cs.toCompletableFuture().complete(null);
        ts.assertCompletedSuccessfully().assertResult(null);
    }


    @Test
    public void testWithNonNullValue() {
        AssertSubscriber<String> ts = AssertSubscriber.create();
        CompletionStage<String> cs = new CompletableFuture<>();
        Uni.fromCompletionStage(cs).subscribe(ts);
        cs.toCompletableFuture().complete("1");
        ts.assertCompletedSuccessfully().assertResult("1");
    }


    @Test
    public void testWithException() {
        AssertSubscriber<String> ts = AssertSubscriber.create();
        CompletionStage<String> cs = new CompletableFuture<>();
        Uni.fromCompletionStage(cs).subscribe(ts);
        cs.toCompletableFuture().completeExceptionally(new IOException("boom"));
        ts.assertFailed(IOException.class, "boom");
    }

    @Test
    public void testThatValueIsNotEmittedBeforeSubscription() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        AtomicBoolean called = new AtomicBoolean();
        CompletableFuture<Integer> cs = new CompletableFuture<>();
        cs.complete(1);
        Uni<Integer> uni = Uni.fromCompletionStage(cs).map(i -> {
            called.set(true);
            return i + 1;
        });


        assertThat(called).isFalse();

        uni.subscribe(ts);
        ts.assertCompletedSuccessfully().assertResult(2);
        assertThat(called).isTrue();
    }

    @Test
    public void testThatSubscriberIsIncompleteIfTheStageDoesNotEmit() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        AtomicBoolean called = new AtomicBoolean();
        CompletableFuture<Integer> cs = new CompletableFuture<>();
        Uni<Integer> uni = Uni.fromCompletionStage(cs).map(i -> {
            called.set(true);
            return i + 1;
        });


        assertThat(called).isFalse();

        uni.subscribe(ts);
        assertThat(called).isFalse();
        ts.assertHasNotBeenCompleted();
    }

    @Test
    public void testThatSubscriberCanCancelBeforeEmission() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        CompletableFuture<Integer> cs = new CompletableFuture<>();
        Uni<Integer> uni = Uni.fromCompletionStage(cs).map(i -> i + 1);

        uni.subscribe(ts);
        ts.cancel();

        cs.complete(1);

        ts.assertHasNotBeenCompleted();
    }

    @Test
    public void testThatSubscriberCanCancelAfterEmission() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        CompletableFuture<Integer> cs = new CompletableFuture<>();
        Uni<Integer> uni = Uni.fromCompletionStage(cs).map(i -> i + 1);

        uni.subscribe(ts);
        cs.complete(1);
        ts.cancel();

        ts.assertResult(2);
    }


}