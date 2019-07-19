package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class UniFailOnNullTest {


    @Test(expected = NoSuchElementException.class)
    public void testFail() {
        Uni.from().nullValue().onNull().fail().await().indefinitely();
    }

    @Test
    public void testFailNotCalledOnResult() {
        assertThat(Uni.from().value(1).onNull().fail().await().indefinitely()).isEqualTo(1);
    }

    @Test(expected = RuntimeException.class)
    public void testFailWithException() {
        Uni.from().nullValue().onNull().failWith(new RuntimeException("boom")).await().indefinitely();
    }

    @Test(expected = NullPointerException.class)
    public void testFailWithExceptionSetToNull() {
        Uni.from().nullValue().onNull().failWith((Exception) null).await().indefinitely();
    }

    @Test
    public void testFailWithExceptionNotCalledOnResult() {
        assertThat(Uni.from().value(1).onNull().failWith(new IOException("boom")).await().indefinitely()).isEqualTo(1);
    }

    @Test
    public void testFailWithExceptionSupplier() {
        AtomicInteger count = new AtomicInteger();
        Uni<Void> boom = Uni.from().nullValue().onNull().failWith(() -> new RuntimeException(Integer.toString(count.incrementAndGet())));

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> boom.await().indefinitely()).withMessageEndingWith("1");
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> boom.await().indefinitely()).withMessageEndingWith("2");
    }

    @Test(expected = NullPointerException.class)
    public void testFailWithExceptionSupplierSetToNull() {
        Uni.from().nullValue().onNull().failWith((Supplier<Throwable>) null).await().indefinitely();
    }

    @Test
    public void testFailWithExceptionSupplierNotCalledOnResult() {
        assertThat(Uni.from().value(1).onNull().failWith(new IOException("boom")).await().indefinitely()).isEqualTo(1);
    }


}