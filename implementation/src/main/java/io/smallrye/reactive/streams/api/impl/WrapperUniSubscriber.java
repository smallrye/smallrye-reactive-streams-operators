package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An implementation of {@link UniSubscriber} and {@link UniSubscription} making sure signals are only called once.
 */
public class WrapperUniSubscriber<T> implements UniSubscriber<T>, UniSubscription {

    private static final int INIT = 0;
    private static final int SUBSCRIBED = 1;
    private static final int HAS_SUBSCRIPTION = 2;
    private static final int DONE = 3; // Terminal state

    private final AtomicInteger state = new AtomicInteger(INIT);
    private final UniImpl<T> source;
    private final UniSubscriber<? super T> downstream;
    private UniSubscription upstream;

    public static <T> void subscribing(UniImpl<T> source, UniSubscriber<? super  T> subscriber) {
        WrapperUniSubscriber<T> wrapped = new WrapperUniSubscriber<>(source, subscriber);
        wrapped.subscribe();

    }

    private WrapperUniSubscriber(UniImpl<T> source, UniSubscriber<? super T> subscriber) {
        this.source = Objects.requireNonNull(source, "`source` must not be `null`");
        this.downstream = Objects.requireNonNull(subscriber, "`subscriber` must not be `null`");
    }

    private void subscribe() {
        if (state.compareAndSet(INIT, SUBSCRIBED)) {
            this.source.subscribing(this);
        } else {
            this.downstream.onSubscribe(EmptySubscription.INSTANCE);
            this.downstream.onFailure(new IllegalStateException("Unable to subscribe, already got a subscriber"));
        }
    }

    @Override
    public void onSubscribe(UniSubscription subscription) {
        Objects.requireNonNull(subscription, "`subscription` must not be`null`");
        if (state.compareAndSet(SUBSCRIBED, HAS_SUBSCRIPTION)) {
            this.upstream = subscription;
            this.downstream.onSubscribe(this);
        } else {
            this.downstream.onSubscribe(EmptySubscription.INSTANCE);
            this.downstream.onFailure(new IllegalStateException("Invalid transition, expected to be in the SUBSCRIBED state but was in " + state.get()));
        }
    }

    @Override
    public void onResult(T result) {
        if (state.compareAndSet(HAS_SUBSCRIPTION, DONE)) {
            downstream.onResult(result);
            cleanup();
        } else if(state.get() != DONE) { // Are we already done? In this case, drop the signal
            downstream.onSubscribe(EmptySubscription.INSTANCE);
            downstream.onFailure(new IllegalStateException("Invalid transition, expected to be in the HAS_SUBSCRIPTION state but was in " + state.get()));
        }
    }

    @Override
    public void onFailure(Throwable failure) {
        if (state.compareAndSet(HAS_SUBSCRIPTION, DONE)) {
            downstream.onFailure(failure);
        } else if(state.get() != DONE) { // Are we already done? In this case, drop the signal
            downstream.onSubscribe(EmptySubscription.INSTANCE);
            downstream.onFailure(new IllegalStateException("Invalid transition, expected to be in the HAS_SUBSCRIPTION state but was in " + state.get()));
        }
    }

    private void cleanup() {
        upstream = null;
    }

    @Override
    public void cancel() {
        if (state.compareAndSet(HAS_SUBSCRIPTION, DONE)) {
            upstream.cancel();
            cleanup();
        }
    }
}
