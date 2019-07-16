package io.smallrye.reactive.streams.api.impl;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class UniFromPublisherTest {

    @Test
    public void testWithPublisher() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        Uni.fromPublisher(Flowable.just(1)).subscribe().withSubscriber(ts);
        ts.assertCompletedSuccessfully().assertResult(1);
    }

    @Test
    public void testWithPublisherBuilder() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        Uni.fromPublisher(ReactiveStreams.of(1)).subscribe().withSubscriber(ts);
        ts.assertCompletedSuccessfully().assertResult(1);
    }

    @Test
    public void testWithMultiValuedPublisher() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        AtomicBoolean cancelled = new AtomicBoolean();
        Uni.fromPublisher(Flowable.just(1, 2, 3).doOnCancel(() -> cancelled.set(true))).subscribe().withSubscriber(ts);
        ts.assertCompletedSuccessfully().assertResult(1);
        assertThat(cancelled).isTrue();
    }


    @Test
    public void testWithException() {
        AssertSubscriber<Object> ts = AssertSubscriber.create();
        Uni.fromPublisher(ReactiveStreams.failed(new IOException("boom"))).subscribe().withSubscriber(ts);
        ts.assertFailure(IOException.class, "boom");
    }

    @Test
    public void testWithEmptyStream() {
        AssertSubscriber<Object> ts = AssertSubscriber.create();
        Uni.fromPublisher(ReactiveStreams.empty()).subscribe().withSubscriber(ts);
        ts.assertCompletedSuccessfully().assertResult(null);
    }

    @Test
    public void testThatValueIsNotEmittedBeforeSubscription() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        AtomicBoolean called = new AtomicBoolean();
        Uni<Integer> uni = Uni.fromPublisher(Flowable.generate(emitter -> {
            called.set(true);
            emitter.onNext(1);
            emitter.onComplete();
        }));


        assertThat(called).isFalse();
        uni.subscribe().withSubscriber(ts);
        ts.assertCompletedSuccessfully().assertResult(1);
        assertThat(called).isTrue();
    }

    @Test
    public void testThatSubscriberIsIncompleteIfThePublisherDoesNotEmit() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        AtomicBoolean called = new AtomicBoolean();
        Uni<Integer> uni = Uni.<Integer>fromPublisher(Flowable.never()).map(i -> {
            called.set(true);
            return i + 1;
        });

        assertThat(called).isFalse();

        uni.subscribe().withSubscriber(ts);
        assertThat(called).isFalse();
        ts.assertNotCompleted();
    }

    @Test
    public void testThatSubscriberCanCancelBeforeEmission() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        Uni<Integer> uni = Uni.fromPublisher(Flowable.<Integer>create(emitter -> {
            new Thread(() -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                emitter.onNext(1);
            }).start();
        }, BackpressureStrategy.DROP)).map(i -> i + 1);

        uni.subscribe().withSubscriber(ts);
        ts.cancel();

        ts.assertNotCompleted();
    }


}