package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.UniSubscription;

/**
 * An implementation of subscription ignoring all calls.
 * This implementation should be accessed using its singleton instance.
 */
public class EmptySubscription implements UniSubscription {

    static final UniSubscription CANCELLED = new EmptySubscription();

    public static final EmptySubscription INSTANCE = new EmptySubscription();

    private EmptySubscription() {
        // Avoid direct instantiation.
    }


    @Override
    public void cancel() {
        // Ignored.
    }
}
