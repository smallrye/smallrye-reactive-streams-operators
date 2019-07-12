package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.UniSubscription;

/**
 * An implementation of subscription ignoring all calls.
 * This implementaton should be accessed using its singleton instance.
 */
public class EmptySubscription implements UniSubscription {

    public static EmptySubscription INSTANCE = new EmptySubscription();

    private EmptySubscription() {
        // Avoid direct instantiation.
    }


    @Override
    public void cancel() {
        // Ignored.
    }
}
