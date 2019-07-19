package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class UniSwitchOnNullTest {

    private Uni<Integer> fallback = Uni.of(23);
    private Uni<Integer> failure = Uni.from().failure(new IOException("boom"));

    @Test
    public void testSwitchToFallback() {
        assertThat(Uni.from().nullValue().map().to(Integer.class)
                .onNull().switchTo(fallback)
                .await().indefinitely()).isEqualTo(23);
    }

    @Test
    public void testSwitchToSupplierFallback() {
        AtomicInteger count = new AtomicInteger();
        assertThat(Uni.from().nullValue().map().to(Integer.class)
                .onNull().switchTo(() -> fallback.map(i -> i + count.incrementAndGet()))
                .await().indefinitely()).isEqualTo(24);

        assertThat(Uni.from().nullValue().map().to(Integer.class)
                .onNull().switchTo(() -> fallback.map(i -> i + count.incrementAndGet()))
                .await().indefinitely()).isEqualTo(25);
    }

    @Test
    public void testSwitchToFailure() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() ->
                        Uni.from().nullValue().map().to(Integer.class)
                                .onNull().switchTo(failure)
                                .await().indefinitely()
                ).withMessageEndingWith("boom");

    }

    @Test
    public void testSwitchToSupplierFailure() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() ->
                        Uni.from().nullValue().map().to(Integer.class)
                                .onNull().switchTo(() -> failure)
                                .await().indefinitely()
                ).withMessageEndingWith("boom");

    }

    @Test(expected = NullPointerException.class)
    public void testSwitchToNull() {
        Uni.from().nullValue().map().to(Integer.class)
                .onNull().switchTo((Uni<Integer>) null)
                .await().indefinitely();
    }

    @Test(expected = NullPointerException.class)
    public void testSwitchToNullSupplier() {
        Uni.from().nullValue().map().to(Integer.class)
                .onNull().switchTo((Uni<? extends Integer>) null)
                .await().indefinitely();
    }

}