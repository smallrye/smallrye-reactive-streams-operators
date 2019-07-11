package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

public class IgnoringUniSubscriber<T> implements UniSubscriber<T> {


    @Override
    public void onSubscribe(UniSubscription subscription) {
        // Ignored.
    }

    @Override
    public void onResult(T result) {
        // Ignored.
    }

    @Override
    public void onFailure(Throwable t) {
        // Ignored.
    }
}
