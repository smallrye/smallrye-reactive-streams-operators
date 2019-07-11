package io.smallrye.reactive.streams.api;

import org.reactivestreams.Subscription;

/**
 * A {@link Subscription} for the {@link Uni} type.
 * The main different with the Reactive Streams Subscription is about the request protocol. Uni does not use
 * request and trigger the computation at subscription time.
 */
public interface UniSubscription extends Subscription {

    /**
     * Requests the {@link Uni} to cancel and clean up resources.
     * If the result is retrieved after cancellation, it is not forwarded to the subscriber.
     * If the cancellation happens after the delivery of the result, this call is ignored.
     */
    void cancel();

    @Override
    default void request(long n) {
        if (n < 1) {
            throw new IllegalArgumentException("Invalid request");
        }
        // Ignored, on Uni the request happen at subscription time.
    }
}
