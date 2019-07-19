package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class UniMapToBooleanTest {

    private Uni<Integer> one = Uni.of(1);

    @Test(expected = NullPointerException.class)
    public void testThatPredicateCannotBeNull() {
        one = Uni.of(1);
        one.map().toBoolean(null);
    }

    @Test
    public void testPassingThePredicate() {
        one.map().toBoolean(i -> i == 1).subscribe().<AssertSubscriber<Boolean>>withSubscriber(AssertSubscriber.create())
                .assertCompletedSuccessfully().assertResult(true);
    }

    @Test
    public void testNotPassingThePredicate() {
        one.map().toBoolean(i -> i == -1).subscribe().<AssertSubscriber<Boolean>>withSubscriber(AssertSubscriber.create())
                .assertCompletedSuccessfully().assertResult(false);
    }

    @Test
    public void testCalledWithNull() {
        Uni.from().nullValue().map().toBoolean(Objects::nonNull).subscribe().<AssertSubscriber<Boolean>>withSubscriber(AssertSubscriber.create())
                .assertCompletedSuccessfully().assertResult(false);

        Uni.from().nullValue().map().toBoolean(Objects::isNull).subscribe().<AssertSubscriber<Boolean>>withSubscriber(AssertSubscriber.create())
                .assertCompletedSuccessfully().assertResult(true);
    }

    @Test
    public void testWithPredicateThrowingException() {
        one.map().toBoolean(x -> {
            throw new IllegalArgumentException("boom");
        }).subscribe().<AssertSubscriber<Boolean>>withSubscriber(AssertSubscriber.create())
                .assertCompletedWithFailure()
                .assertFailure(IllegalArgumentException.class, "boom");
    }

    @Test
    public void testPredicateNotCalledOnFailure() {
        AtomicBoolean called = new AtomicBoolean();
        one
                .map(i -> {
                    throw new IllegalArgumentException("boom");
                })
                .map().toBoolean(i -> {
                    called.set(true);
                    return true;
        }).subscribe().<AssertSubscriber<Boolean>>withSubscriber(AssertSubscriber.create())
                .assertCompletedWithFailure()
                .assertFailure(IllegalArgumentException.class, "boom");

        Assertions.assertThat(called).isFalse();
    }

}
