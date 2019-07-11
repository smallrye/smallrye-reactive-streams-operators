package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class OfUniOperator<O> extends UniOperator<Void, O> {
    private final O value;

    public OfUniOperator(O value) {
        super(null);
        this.value = value;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public OfUniOperator(Optional<O> value) {
        super(null);
        this.value = Objects.requireNonNull(value,"`value` must not be `null`").orElse(null);
    }

    @Override
    public void subscribe(UniSubscriber<? super O> subscriber) {
        AtomicBoolean cancelled = new AtomicBoolean();
        UniSubscription subscription = () -> cancelled.set(true);
        subscriber.onSubscribe(subscription);
        if (!cancelled.get()) {
            subscriber.onResult(value);
        }
    }
}
