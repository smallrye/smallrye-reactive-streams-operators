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
        Uni<Integer> s =  Uni.from().deferred(() -> Uni.of(counter.incrementAndGet()));

        for (int i = 1; i < 100; i++) {
            assertThat(s.await().indefinitely()).isEqualTo(i);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testWithNull() {
         Uni.from().deferred(null);
    }

    @Test
    public void testWithASupplierProducingNull() {
        Uni<Integer> s =  Uni.from().deferred(() -> null);
        AssertSubscriber<Integer> subscriber = AssertSubscriber.create();
        s.subscribe().withSubscriber(subscriber);
        subscriber.assertFailure(NullPointerException.class, "");
    }

    @Test
    public void testWithASupplierThrowingAnException() {
        Uni<Integer> s =  Uni.from().deferred(() -> {
            throw new IllegalStateException("boom");
        });
        AssertSubscriber<Integer> subscriber = AssertSubscriber.create();
        s.subscribe().withSubscriber(subscriber);
        subscriber.assertFailure(IllegalStateException.class, "boom");
    }
}
