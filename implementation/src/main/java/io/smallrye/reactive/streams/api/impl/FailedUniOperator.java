package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.UniSubscriber;

import java.util.concurrent.atomic.AtomicBoolean;

public class FailedUniOperator<O> extends UniOperator<Void, O> {
    private final Throwable throwable;

    public FailedUniOperator(Throwable throwable) {
        super(null);
        this.throwable = throwable;
    }

    @Override
    public void subscribe(UniSubscriber<? super O> subscriber) {
        AtomicBoolean cancelled = new AtomicBoolean();
        subscriber.onSubscribe(() -> cancelled.compareAndSet(false, true));
        if (!cancelled.get()) {
            subscriber.onFailure(throwable);
        }

    }
}
