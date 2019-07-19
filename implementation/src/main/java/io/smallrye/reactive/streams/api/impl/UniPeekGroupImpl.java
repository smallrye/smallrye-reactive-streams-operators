package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscription;
import io.smallrye.reactive.streams.api.groups.UniPeekGroup;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class UniPeekGroupImpl<T> implements UniPeekGroup<T> {

    private final DefaultUni<T> source;

    public UniPeekGroupImpl(DefaultUni<T> source) {
        this.source = nonNull(source, "source");
    }

    @Override
    public Uni<T> terminate(BiConsumer<? super T, Throwable> callback) {
        return new UniActions<>(source, null, null, null, null,
                nonNull(callback, "callback"));
    }

    @Override
    public Uni<T> cancellation(Runnable callback) {
        return new UniActions<>(source, null, null, null,
                nonNull(callback, "callback"),
                null);
    }

    @Override
    public Uni<T> failure(Consumer<Throwable> callback) {
        return new UniActions<>(source, null, null,
                nonNull(callback, "callback"),
                null, null);
    }

    @Override
    public Uni<T> result(Consumer<? super T> callback) {
        return new UniActions<>(source, null,
                nonNull(callback, "callback"),
                null, null, null);
    }

    @Override
    public Uni<T> subscribe(Consumer<? super UniSubscription> callback) {
        return new UniActions<>(source, nonNull(callback, "callback"),
                null, null, null, null);
    }
}
