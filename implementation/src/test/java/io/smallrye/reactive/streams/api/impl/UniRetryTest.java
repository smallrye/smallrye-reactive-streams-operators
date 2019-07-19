package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class UniRetryTest {

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidNumberOfAttempts() {
        Uni.from().nothing().recover().withRetry().atMost(-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidNumberOfAttemptsWithZero() {
        Uni.from().nothing().recover().withRetry().atMost(0);
    }

    @Test
    public void testNoRetryOnResult() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        Uni.of(1)
                .recover().withRetry().atMost(1)
                .subscribe().withSubscriber(ts);
        ts.assertResult(1);

    }

    @Test
    public void testWithOneRetry() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();

        AtomicInteger count = new AtomicInteger();
        Uni.from().value(() -> {
            int i = count.getAndIncrement();
            if (i < 1) {
                throw new RuntimeException("boom");
            }
            return i;
        })
                .recover().withRetry().atMost(1)
                .subscribe().withSubscriber(ts);

        ts
                .assertCompletedSuccessfully()
                .assertResult(1);

    }

    @Test
    public void testWithInfiniteRetry() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        AtomicInteger count = new AtomicInteger();
        Uni.from().value(() -> {
            int i = count.getAndIncrement();
            if (i < 10) {
                throw new RuntimeException("boom");
            }
            return i;
        })
                .recover().withRetry().indefinitely()
                .subscribe().withSubscriber(ts);

        ts
                .assertCompletedSuccessfully()
                .assertResult(10);
    }

    @Test
    public void testWithMapperFailure() {
        AtomicInteger count = new AtomicInteger();
        Uni.of(1)
                .on().result(input -> {
                    if (count.incrementAndGet() < 2) {
                        throw new RuntimeException("boom");
                    }
                })
                .recover().withRetry().atMost(2)
                .subscribe().<AssertSubscriber<Integer>>withSubscriber(AssertSubscriber.create())
                .assertResult(1);
    }
}
