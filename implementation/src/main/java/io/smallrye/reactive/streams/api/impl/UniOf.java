package io.smallrye.reactive.streams.api.impl;

import java.util.Objects;
import java.util.Optional;

public class UniOf<O> extends UniOperator<Void, O> {

    private final O value;

    public UniOf(O value) {
        super(null);
        this.value = value;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public UniOf(Optional<O> value) {
        super(null);
        this.value = Objects.requireNonNull(value, "`value` must not be `null`").orElse(null);
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super O> subscriber) {
        // Use the empty subscription as cancellation would not have any effect,
        // Immediate cancellation is managed by the Wrapper.
        subscriber.onSubscribe(EmptySubscription.INSTANCE);
        subscriber.onResult(value);
    }
}
