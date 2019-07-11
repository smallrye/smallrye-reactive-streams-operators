package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class OfUniOperatorTest {

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

}