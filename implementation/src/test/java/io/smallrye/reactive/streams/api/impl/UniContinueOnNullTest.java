package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class UniContinueOnNullTest {

    @Test
    public void testContinue() {
        assertThat(Uni.from().nullValue().map().to(Integer.class)
                .onNull().continueWith(42)
                .await().indefinitely()).isEqualTo(42);
    }

    @Test
    public void testContinueWithSupplier() {
        AtomicInteger counter = new AtomicInteger();
        Uni<Integer> uni = Uni.from().nullValue().map().to(Integer.class)
                .onNull().continueWith(counter::incrementAndGet);
        assertThat(uni.await().indefinitely()).isEqualTo(1);
        assertThat(uni.await().indefinitely()).isEqualTo(2);
    }

    @Test
    public void testContinueNotCalledOnResult() {
        assertThat(Uni.from().value(23).map().to(Integer.class)
                .onNull().continueWith(42)
                .await().indefinitely()).isEqualTo(23);
    }

    @Test
    public void testContinueWithSupplierNotCalledOnResult() {
        assertThat(Uni.from().value(23).map().to(Integer.class)
                .onNull().continueWith(() -> 42)
                .await().indefinitely()).isEqualTo(23);
    }

    @Test
    public void testContinueNotCalledOnFailure() {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                Uni.from().failure(new IOException("boom")).map().to(Integer.class)
                .onNull().continueWith(42)
                .await().indefinitely()
        ).withCauseExactlyInstanceOf(IOException.class).withMessageEndingWith("boom");
    }

    @Test
    public void testContinueWithSupplierNotCalledOnFailure() {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                Uni.from().failure(new IOException("boom")).map().to(Integer.class)
                        .onNull().continueWith(() -> 42)
                        .await().indefinitely()
        ).withCauseExactlyInstanceOf(IOException.class).withMessageEndingWith("boom");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThatContinueWithCannotUseNull() {
        Uni.from().value(23).map().to(Integer.class)
                .onNull().continueWith((Integer) null);
    }

    @Test(expected = NullPointerException.class)
    public void testThatContinueWithSupplierCannotReturnNull() {
        Uni.from().value(23).map(x -> null)
                .onNull().continueWith(() -> null).await().indefinitely();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThatContinueWithSupplierCannotBeNull() {
        Uni.from().value(23).map().to(Integer.class)
                .onNull().continueWith((Supplier<Integer>) null);
    }



}