package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;

public class UniAnyTest {

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    @After
    public void shutdown() {
        executor.shutdown();
    }


    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void testWithNullAsIterable() {
        Uni.any((Iterable) null);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void testWithNullAsArray() {
        Uni.any((Uni[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testWithItemInIterable() {
        List<Uni<String>> unis = new ArrayList<>();
        unis.add(Uni.of("foo"));
        unis.add(null);
        unis.add(Uni.of("bar"));
        Uni.any(unis);
    }

    @Test(expected = NullPointerException.class)
    public void testWithItemInArray() {
        Uni.any(Uni.of("foo"), null, Uni.of("bar"));
    }

    @Test
    public void testWithNoCandidate() {
        AssertSubscriber<Void> subscriber = AssertSubscriber.create();
        Uni.<Void>any().subscribe(subscriber);
        subscriber.assertCompletedSuccessfully().assertResult(null);
    }

    @Test
    public void testWithSingleItemCompletingSuccessfully() {
        AssertSubscriber<String> subscriber = AssertSubscriber.create();
        Uni.any(Uni.of("foo")).subscribe(subscriber);
        subscriber.assertCompletedSuccessfully().assertResult("foo");
    }

    @Test
    public void testWithSingleItemCompletingWithAFailure() {
        AssertSubscriber<String> subscriber = AssertSubscriber.create();
        Uni.any(Uni.<String>failed(new IOException("boom"))).subscribe(subscriber);
        subscriber.assertCompletedWithFailure().assertFailure(IOException.class, "boom");
    }

    @Test
    public void testWithTwoUnisCompletingImmediately() {
        AssertSubscriber<String> subscriber = AssertSubscriber.create();
        Uni.any(Uni.of("foo"), Uni.of("bar")).subscribe(subscriber);
        subscriber.assertCompletedSuccessfully().assertResult("foo");
    }

    @Test
    public void testWithTwoUnisCompletingWithAFailure() {
        AssertSubscriber<String> subscriber = AssertSubscriber.create();
        Uni.any(Uni.<String>failed(new IOException("boom")), Uni.of("foo")).subscribe(subscriber);
        subscriber.assertCompletedWithFailure().assertFailure(IOException.class, "boom");
    }

    @Test
    public void testWithADelayedUni() {
        AssertSubscriber<String> subscriber1 = AssertSubscriber.create();
        Uni.any(Uni.of("foo").delay(Duration.ofMillis(10), executor), Uni.of("bar")).subscribe(subscriber1);
        subscriber1.assertCompletedSuccessfully().assertResult("bar");

        AssertSubscriber<String> subscriber2 = AssertSubscriber.create();
        Uni.any(Uni.of("foo").delay(Duration.ofMillis(10), executor), Uni.of("bar").delay(Duration.ofMillis(100), executor)).subscribe(subscriber2);
        subscriber2.await().assertCompletedSuccessfully().assertResult("foo");
    }

    @Test(timeout = 1000)
    public void testBlockingWithDelay() {
        Uni<Integer> uni1 = Uni.empty().delay(Duration.ofMillis(100), executor).map(x -> 1);
        Uni<Integer> uni2 = Uni.empty().delay(Duration.ofMillis(50), executor).map(x -> 2);
        assertThat(Uni.any(uni1, uni2).await().indefinitely()).isEqualTo(2);
    }

    @Test(timeout = 1000)
    public void testCompletingAgainstEmpty() {
        Uni<Integer> uni1 = Uni.empty().map(x -> 1);
        Uni<Integer> uni2 = Uni.empty().delay(Duration.ofMillis(50), executor).map(x -> 2);
        assertThat(Uni.any(uni1, uni2).await().indefinitely()).isEqualTo(1);
    }

    @Test(timeout = 1000)
    public void testCompletingAgainstNever() {
        Uni<Integer> uni1 = Uni.never().map(x -> 1);
        Uni<Integer> uni2 = Uni.empty().delay(Duration.ofMillis(50), executor).map(x -> 2);
        assertThat(Uni.any(uni1, uni2).await().asOptional().indefinitely()).contains(2);
    }

    @Test
    public void testWithThreeImmediateChallengers() {
        Uni<Integer> any = Uni.any(Uni.of(1), Uni.of(2), Uni.of(3));

        AssertSubscriber<Integer> subscriber = AssertSubscriber.create();
        any.subscribe(subscriber);
        subscriber.assertCompletedSuccessfully().assertResult(1);
    }

}