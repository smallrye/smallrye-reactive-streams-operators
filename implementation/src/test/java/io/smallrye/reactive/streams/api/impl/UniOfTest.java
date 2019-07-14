package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class UniOfTest {

    @Test
    public void testThatNullValueAreAccepted() {
        AssertSubscriber<Object> ts = AssertSubscriber.create();
        Uni.of(null).subscribe(ts);
        ts.assertCompletedSuccessfully().assertResult(null);
    }


    @Test
    public void testWithNonNullValue() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        Uni.of(1).subscribe(ts);
        ts.assertCompletedSuccessfully().assertResult(1);
    }


    @Test
    public void testThatEmptyIsAcceptedWithFromOptional() {
        AssertSubscriber<Object> ts = AssertSubscriber.create();
        Uni.fromOptional(Optional.empty()).subscribe(ts);
        ts.assertCompletedSuccessfully().assertResult(null);
    }

    @SuppressWarnings("OptionalAssignedToNull")
    @Test(expected = NullPointerException.class)
    public void testThatNullIfNotAcceptedByFromOptional() {
        Uni.fromOptional(null); // Immediate failure, no need for subscription
    }


    @Test
    public void testThatFulfilledOptionalIsAcceptedWithFromOptional() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        Uni.fromOptional(Optional.of(1)).subscribe(ts);
        ts.assertCompletedSuccessfully().assertResult(1);
    }


    @Test
    public void testThatValueIsNotEmittedBeforeSubscription() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        AtomicBoolean called = new AtomicBoolean();
        Uni<Integer> uni = Uni.of(1).map(i -> {
            called.set(true);
            return i + 1;
        });

        assertThat(called).isFalse();

        uni.subscribe(ts);
        ts.assertCompletedSuccessfully().assertResult(2);
        assertThat(called).isTrue();
    }

    @Test
    public void testThatValueIsRetrievedUsingBlock() {
        assertThat(Uni.of("foo").block()).isEqualToIgnoringCase("foo");
    }

    @Test
    public void testWithImmediateCancellation() {
        AssertSubscriber<String> subscriber1 = new AssertSubscriber<>(true);
        AssertSubscriber<String> subscriber2 = new AssertSubscriber<>(false);
        Uni<String> foo = Uni.of("foo");
        foo.subscribe(subscriber1);
        foo.subscribe(subscriber2);
        subscriber1.assertNoResult().assertNoFailure();
        subscriber2.assertCompletedSuccessfully().assertResult("foo");
    }

    @Test
    public void testEmpty() {
        AssertSubscriber<Void> subscriber = AssertSubscriber.create();
        Uni.empty().subscribe(subscriber);
        subscriber.assertCompletedSuccessfully().assertResult(null);
    }

    @Test
    public void testEmptyWithImmediateCancellation() {
        AssertSubscriber<Void> subscriber = new AssertSubscriber<>(true);
        Uni.empty().subscribe(subscriber);
        subscriber.assertNoFailure().assertNoResult();
    }

}