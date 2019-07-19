package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.CompositeException;
import io.smallrye.reactive.streams.api.TimeoutException;
import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.tuples.Pair;
import io.smallrye.reactive.streams.api.tuples.Tuple3;
import io.smallrye.reactive.streams.api.tuples.Tuple4;
import io.smallrye.reactive.streams.api.tuples.Tuple5;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class AndUniTest {

    @Test
    public void testWithTwoSimpleUnis() {

        Uni<Integer> uni = Uni.from().value(1);
        Uni<Integer> uni2 = Uni.from().value(2);

        AssertSubscriber<Pair<Integer, Integer>> subscriber =
                uni.and().uni(uni2).asPair().subscribe().withSubscriber(AssertSubscriber.create());

        assertThat(subscriber.getResult().asList()).containsExactly(1, 2);
    }

    @Test
    public void testWithTwoOneFailure() {
        AssertSubscriber<Pair<Integer, Integer>> subscriber =
                Uni.from().value(1).and().uni(Uni.from().<Integer>failure(new IOException("boom"))).asPair()
                        .subscribe().withSubscriber(AssertSubscriber.create());
        subscriber.assertFailure(IOException.class, "boom");
    }

    @Test(expected = TimeoutException.class)
    public void testWithNever() {
        Uni<Tuple3<Integer, Integer, Object>> tuple = Uni.from().value(1).and().unis(Uni.of(2), Uni.from().nothing()).asTuple();
        tuple.await().atMost(Duration.ofMillis(1000));
    }

    @Test
    public void testWithTwoFailures() {
        AssertSubscriber<Tuple3<Integer, Integer, Integer>> subscriber =
                Uni.from().value(1).and().unis(
                        Uni.from().<Integer>failure(new IOException("boom")),
                        Uni.from().<Integer>failure(new IOException("boom 2")))
                        .awaitCompletion()
                        .asTuple()
                        .subscribe().withSubscriber(AssertSubscriber.create());
        subscriber.assertCompletedWithFailure()
                .assertFailure(CompositeException.class, "boom")
                .assertFailure(CompositeException.class, "boom 2");
    }

    @Test
    public void testWithCombinator() {
        Uni<Integer> uni1 = Uni.from().value(1);
        Uni<Integer> uni2 = Uni.from().value(2);
        Uni<Integer> uni3 = Uni.from().value(3);
        AssertSubscriber<Integer> subscriber =
                uni1.and().unis(uni2, uni3)
                        .combinedWith((i1, i2, i3) -> i1 + i2 + i3)
                        .subscribe().withSubscriber(AssertSubscriber.create());
        subscriber.await().assertResult(6);
    }

    @Test
    public void testTerminationJoin() {
        Uni<Void> uni = Uni.of(1).and(Uni.of("hello")).ignore().andContinueWithNull();

        uni.subscribe().<AssertSubscriber<Void>>withSubscriber(AssertSubscriber.create())
                .assertCompletedSuccessfully()
                .assertResult(null);
    }

    @Test
    public void testWithFiveUnis() {
        Uni<Integer> uni = Uni.from().value(1);
        Uni<Integer> uni2 = Uni.from().value(2);
        Uni<Integer> uni3 = Uni.from().value(3);
        Uni<Integer> uni4 = Uni.from().value(4);

        AssertSubscriber<Tuple5<Integer, Integer, Integer, Integer, Integer>> subscriber =
                uni.and().unis(uni, uni2, uni3, uni4).asTuple().subscribe().withSubscriber(AssertSubscriber.create());

        assertThat(subscriber.getResult().asList()).containsExactly(1, 1, 2, 3, 4);
    }

    @Test
    public void testWithFourUnis() {
        Uni<Integer> uni = Uni.from().value(1);
        Uni<Integer> uni2 = Uni.from().value(2);
        Uni<Integer> uni3 = Uni.from().value(3);

        AssertSubscriber<Tuple4<Integer, Integer, Integer, Integer>> subscriber =
                uni.and().unis(uni, uni2, uni3).asTuple().subscribe().withSubscriber(AssertSubscriber.create());

        assertThat(subscriber.getResult().asList()).containsExactly(1, 1, 2, 3);
    }


}