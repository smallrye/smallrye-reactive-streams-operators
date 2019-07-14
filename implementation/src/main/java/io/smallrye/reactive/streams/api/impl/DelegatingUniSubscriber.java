package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.Objects;

public class DelegatingUniSubscriber<O> implements UniSubscriber<O> {

    private final UniSubscriber<O> delegate;

    public DelegatingUniSubscriber(UniSubscriber<O> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "`delegate` must not be `null`");
    }

    @Override
    public void onSubscribe(UniSubscription subscription) {
        delegate.onSubscribe(subscription);
    }

    @Override
    public void onResult(O result) {
        delegate.onResult(result);
    }

    @Override
    public void onFailure(Throwable failure) {
        delegate.onFailure(failure);
    }
}
