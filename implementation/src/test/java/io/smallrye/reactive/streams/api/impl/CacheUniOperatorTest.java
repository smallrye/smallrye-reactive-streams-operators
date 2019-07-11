package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheUniOperatorTest {


    @Test(expected = NullPointerException.class)
    public void testThatSourceCannotBeNull() {
        new CacheUniOperator<>(null);
    }


    @Test
    public void testThatImmediateValueAreCached() {
        AtomicInteger counter = new AtomicInteger();
        Uni<Integer> cache = Uni.of(counter.incrementAndGet()).cache();

        AssertSubscriber<Integer> sub1 = AssertSubscriber.create();
        AssertSubscriber<Integer> sub2 = AssertSubscriber.create();
        AssertSubscriber<Integer> sub3 = AssertSubscriber.create();

        cache.subscribe(sub1);
        cache.subscribe(sub2);
        cache.subscribe(sub3);

        sub1.assertCompletedSuccessfully().assertResult(1);
        sub2.assertCompletedSuccessfully().assertResult(1);
        sub3.assertCompletedSuccessfully().assertResult(1);
    }

    @Test
    public void testThatIFailureAreCached() {
        AtomicInteger counter = new AtomicInteger();
        Uni<Object> cache = Uni.failed(new Exception("" + counter.getAndIncrement())).cache();

        AssertSubscriber<Object> sub1 = AssertSubscriber.create();
        AssertSubscriber<Object> sub2 = AssertSubscriber.create();
        AssertSubscriber<Object> sub3 = AssertSubscriber.create();

        cache.subscribe(sub1);
        cache.subscribe(sub2);
        cache.subscribe(sub3);

        sub1.assertFailed(Exception.class, "0");
        sub2.assertFailed(Exception.class, "0");
        sub3.assertFailed(Exception.class, "0");
    }

    @Test
    public void testThatValueEmittedAfterSubscriptionAreCached() {
        CompletableFuture<Integer> cs = new CompletableFuture<>();
        Uni<Integer> cache = Uni.fromCompletionStage(cs).cache();

        AssertSubscriber<Integer> sub1 = AssertSubscriber.create();
        AssertSubscriber<Integer> sub2 = AssertSubscriber.create();
        AssertSubscriber<Integer> sub3 = AssertSubscriber.create();

        cache.subscribe(sub1);
        cache.subscribe(sub2);

        cs.complete(1);

        cache.subscribe(sub3);

        sub1.assertCompletedSuccessfully().assertResult(1);
        sub2.assertCompletedSuccessfully().assertResult(1);
        sub3.assertCompletedSuccessfully().assertResult(1);
    }

    @Test
    public void testThatSubscriberCanCancelTheirSubscriptionBeforeReceivingAValue() {
        CompletableFuture<Integer> cs = new CompletableFuture<>();
        Uni<Integer> cache = Uni.fromCompletionStage(cs).cache();

        AssertSubscriber<Integer> sub1 = AssertSubscriber.create();
        AssertSubscriber<Integer> sub2 = AssertSubscriber.create();
        AssertSubscriber<Integer> sub3 = AssertSubscriber.create();

        cache.subscribe(sub1);
        cache.subscribe(sub2);

        sub2.cancel();

        cs.complete(1);

        cache.subscribe(sub3);

        sub1.assertCompletedSuccessfully().assertResult(1);
        sub2.assertHasNotBeenCompleted();
        sub3.assertCompletedSuccessfully().assertResult(1);
    }

    @Test
    public void testThatSubscriberCanCancelTheirSubscriptionAfterHavingReceivingAValue() {
        CompletableFuture<Integer> cs = new CompletableFuture<>();
        Uni<Integer> cache = Uni.fromCompletionStage(cs).cache();

        AssertSubscriber<Integer> sub1 = AssertSubscriber.create();
        AssertSubscriber<Integer> sub2 = AssertSubscriber.create();
        AssertSubscriber<Integer> sub3 = AssertSubscriber.create();

        cache.subscribe(sub1);
        cache.subscribe(sub2);

        cs.complete(1);
        sub2.cancel();


        cache.subscribe(sub3);

        sub1.assertCompletedSuccessfully().assertResult(1);
        sub2.assertCompletedSuccessfully().assertResult(1);
        sub3.assertCompletedSuccessfully().assertResult(1);
    }

}