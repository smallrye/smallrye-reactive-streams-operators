package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.TimeoutException;
import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class UniTimeoutTest {

    @Test
    public void testResultWhenTimeoutIsNotReached() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();

        Uni.of(1)
                .onTimeout().of(Duration.ofMillis(10)).recover().withUni(Uni.from().nothing())
                .subscribe().withSubscriber(ts);

        ts.await().assertCompletedSuccessfully().assertResult(1);
    }

    @Test
    public void testTimeout() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();

        Uni.of(1)
                .delay().of(Duration.ofMillis(10))
                .onTimeout().of(Duration.ofMillis(1)).fail()
                .subscribe().withSubscriber(ts);

        ts.await().assertCompletedWithFailure();
        assertThat(ts.getFailure()).isInstanceOf(TimeoutException.class);

    }

    @Test
    public void testRecoverWithResult() {
        AssertSubscriber<Integer> ts = Uni.from().<Integer>nothing()
                .onTimeout().of(Duration.ofMillis(10)).recover().withResult(5)
                .subscribe().withSubscriber(AssertSubscriber.create());
        ts.await().assertResult(5);
    }

    @Test
    public void testRecoverWithResultSupplier() {
        AssertSubscriber<Integer> ts = Uni.from().<Integer>nothing()
                .onTimeout().of(Duration.ofMillis(10)).recover().withResult(() -> 23)
                .subscribe().withSubscriber(AssertSubscriber.create());
        ts.await().assertResult(23);
    }

    @Test
    public void testRecoverWithSwitchToUni() {
        AssertSubscriber<Integer> ts = Uni.from().<Integer>nothing()
                .onTimeout().of(Duration.ofMillis(10)).recover().withUni(() -> Uni.of(15))
                .subscribe().withSubscriber(AssertSubscriber.create());
        ts.await().assertResult(15);
    }

    @Test
    public void testFailingWithAnotherException() {
        AssertSubscriber<Integer> ts = Uni.from().<Integer>nothing()
                .onTimeout().of(Duration.ofMillis(10)).failWith(new IOException("boom"))
                .subscribe().withSubscriber(AssertSubscriber.create());
        ts.await().assertFailure(IOException.class, "boom");
    }

    @Test
    public void testDurationValidity() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> Uni.of(1).onTimeout().of(null))
                .withMessageContaining("timeout");

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> Uni.of(1).onTimeout().of(Duration.ofMillis(0)))
                .withMessageContaining("timeout");

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> Uni.of(1).onTimeout().of(Duration.ofMillis(-1)))
                .withMessageContaining("timeout");
    }



}
