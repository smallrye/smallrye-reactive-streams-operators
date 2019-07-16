package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.UniAwait;
import io.smallrye.reactive.streams.api.UniAwaitOptional;

import java.time.Duration;
import java.util.Optional;

public class UniAwaitOptionalImpl<T> implements UniAwaitOptional<T> {

    private final UniAwait<T> delegate;

    public UniAwaitOptionalImpl(UniAwait<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<T> indefinitely() {
        return atMost(null);
    }

    @Override
    public Optional<T> atMost(Duration timeout) {
        return Optional.ofNullable(delegate.atMost(timeout));
    }

}
