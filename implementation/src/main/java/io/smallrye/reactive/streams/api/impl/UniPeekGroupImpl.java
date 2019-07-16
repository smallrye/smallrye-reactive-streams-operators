package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniPeekGroup;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class UniPeekGroupImpl<T> implements UniPeekGroup<T> {

    private final DefaultUni<T> source;

    public UniPeekGroupImpl(DefaultUni<T> source) {
        this.source = Objects.requireNonNull(source, "`source` must not be `null`");
    }

    @Override
    public Uni<T> terminate(BiConsumer<? super T, Throwable> callback) {
        return new UniActions<>(source, null, null, null, null,
                Objects.requireNonNull(callback, "`callback` must not be `null`"));
    }

    @Override
    public Uni<T> cancellation(Runnable callback) {
        return new UniActions<>(source, null, null, null,
                Objects.requireNonNull(callback, "`callback` must not be `null`"),
                null);
    }

    @Override
    public Uni<T> failure(Consumer<Throwable> callback) {
        return new UniActions<>(source, null, null,
                Objects.requireNonNull(callback, "`callback` must not be `null`"),
                null, null);
    }

    @Override
    public Uni<T> result(Consumer<? super T> callback) {
        return new UniActions<>(source, null,
                Objects.requireNonNull(callback, "`callback` must not be `null`"),
                null, null, null);
    }

    @Override
    public Uni<T> subscribe(Consumer<? super UniSubscription> callback) {
        return new UniActions<>(source, Objects.requireNonNull(callback, "`callback` must not be `null`"),
                null, null, null, null);
    }
}
