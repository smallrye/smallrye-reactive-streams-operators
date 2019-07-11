package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscription;
import org.reactivestreams.Subscription;

import java.util.Objects;

public abstract class UniOperator<I, O> extends UniImpl<O> {

    private final Uni<? extends I> source;

    public UniOperator(Uni<? extends I> source) {
        // Can be `null`
        this.source = source;
    }

    public Uni<? extends I> source() {
        return source;
    }

    public static class DelegatingUniSubscription implements UniSubscription {

        private final Subscription subscription;

        public DelegatingUniSubscription(Subscription subscription) {
            this.subscription = Objects.requireNonNull(subscription, "`subscription` must not be `null`");
        }

        @Override
        public void cancel() {
            subscription.cancel();
        }
    }

}
