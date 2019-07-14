package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class UniDeferTest {

    @Test
    public void testWithMultipleSubscriptions() {
        AtomicInteger counter = new AtomicInteger();
        Uni<Integer> s = Uni.defer(() -> Uni.of(counter.incrementAndGet()));

        for (int i = 1; i < 100; i++) {
            assertThat(s.block()).isEqualTo(i);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testWithNull() {
        Uni.defer(null);
    }

    @Test
    public void testWithASupplierProducingNull() {
        Uni<Integer> s = Uni.defer(() -> null);
        AssertSubscriber<Integer> subscriber = AssertSubscriber.create();
        s.subscribe(subscriber);
        subscriber.assertFailure(NullPointerException.class, "");
    }

    @Test
    public void testWithASupplierThrowingAnException() {
        Uni<Integer> s = Uni.defer(() -> {
            throw new IllegalStateException("boom");
        });
        AssertSubscriber<Integer> subscriber = AssertSubscriber.create();
        s.subscribe(subscriber);
        subscriber.assertFailure(IllegalStateException.class, "boom");
    }
}
