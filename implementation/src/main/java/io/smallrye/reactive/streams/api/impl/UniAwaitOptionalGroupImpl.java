package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.groups.UniAwaitGroup;
import io.smallrye.reactive.streams.api.groups.UniAwaitOptionalGroup;

import java.time.Duration;
import java.util.Optional;

public class UniAwaitOptionalGroupImpl<T> implements UniAwaitOptionalGroup<T> {

    private final UniAwaitGroup<T> delegate;

    public UniAwaitOptionalGroupImpl(UniAwaitGroup<T> delegate) {
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
