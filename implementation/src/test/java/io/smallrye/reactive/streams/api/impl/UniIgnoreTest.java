package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class UniIgnoreTest {

    @Test
    public void testIgnoreAndContinueWithNull() {
        assertThat(Uni.of(24).ignore().andContinueWithNull().await().indefinitely()).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIgnoreOnFailure() {
        Uni.of(24).map(i -> {
            throw new IllegalArgumentException("BOOM");
        }).ignore().andContinueWithNull().await().indefinitely();
    }

    @Test
    public void testIgnoreAndFail() {
        AssertSubscriber<Integer> subscriber =
                Uni.of(22).ignore().andFail().subscribe().withSubscriber(AssertSubscriber.create());
        subscriber.assertFailure(Exception.class, "");
    }

    @Test
    public void testIgnoreAndFailWith() {
        AssertSubscriber<Integer> subscriber =
                Uni.of(22).ignore().andFail(new IOException("boom")).subscribe().withSubscriber(AssertSubscriber.create());
        subscriber.assertFailure(IOException.class, "boom");
    }

    @Test
    public void testIgnoreAndFailWithSupplier() {
        AtomicInteger count = new AtomicInteger();
        Uni<Integer> boom = Uni.of(22).ignore().andFail(() -> new IOException("boom " + count.incrementAndGet()));
        AssertSubscriber<Integer> s1 =  boom.subscribe().withSubscriber(AssertSubscriber.create());
        AssertSubscriber<Integer> s2 =  boom.subscribe().withSubscriber(AssertSubscriber.create());
        s1.assertFailure(IOException.class, "boom 1");
        s2.assertFailure(IOException.class, "boom 2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIgnoreAndFailWithWithNullFailure() {
        Uni.of(22).ignore().andFail((Exception) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIgnoreAndFailWithWithNullSupplier() {
        Uni.of(22).ignore().andFail((Supplier<Throwable>) null);
    }

    @Test
    public void testIgnoreAndContinueWithValue() {
        assertThat(Uni.of(24).ignore().andContinueWith(42).await().indefinitely()).isEqualTo(42);
    }

    @Test
    public void testIgnoreAndContinueWithValueSupplier() {
        AtomicInteger count = new AtomicInteger();
        Uni<Integer> uni = Uni.of(24).ignore().andContinueWith(count::incrementAndGet);
        assertThat(uni.await().indefinitely()).isEqualTo(1);
        assertThat(uni.await().indefinitely()).isEqualTo(2);
    }

    @Test
    public void testIgnoreAndContinueWithValueSupplierReturningNull() {
        assertThat(Uni.of(24).ignore().andContinueWith(() -> null).await().indefinitely()).isEqualTo(null);
    }

    @Test
    public void testIgnoreAndSwitchToSupplier() {
        AtomicInteger count = new AtomicInteger();
        Uni<Integer> uni = Uni.of(24).ignore().andSwitchTo(() -> Uni.from().value(count::incrementAndGet));
        assertThat(uni.await().indefinitely()).isEqualTo(1);
        assertThat(uni.await().indefinitely()).isEqualTo(2);
    }

    @Test
    public void testIgnoreAndSwitchToUni() {
        AtomicInteger count = new AtomicInteger();
        Uni<Integer> uni = Uni.of(24).ignore().andSwitchTo(Uni.from().value(count::incrementAndGet));
        assertThat(uni.await().indefinitely()).isEqualTo(1);
        assertThat(uni.await().indefinitely()).isEqualTo(2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIgnoreAndSwitchToNullSupplier() {
        Uni.of(22).ignore().andSwitchTo((Supplier<Uni<?>>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIgnoreAndSwitchToNull() {
        Uni.of(22).ignore().andSwitchTo((Uni<?>) null);
    }
}
