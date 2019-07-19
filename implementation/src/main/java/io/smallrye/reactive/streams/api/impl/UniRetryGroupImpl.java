package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.groups.UniRetryGroup;

import java.util.Objects;
import java.util.function.Predicate;

public class UniRetryGroupImpl<T> implements UniRetryGroup<T> {

    private final Uni<T> source;
    private final Predicate<? super Throwable> predicate;

    UniRetryGroupImpl(Uni<T> source, Predicate<? super Throwable> predicate) {
        this.source = Objects.requireNonNull(source, "`source` must not be `null`");
        this.predicate = predicate;
    }

    @Override
    public Uni<T> indefinitely() {
        return atMost(Long.MAX_VALUE);
    }

    @Override
    public Uni<T> atMost(long numberOfAttempts) {
        return new UniRetryWithAttempts<>(source, predicate, numberOfAttempts);

    }

    @Override
    public Uni<T> until(Predicate<? super Throwable> until) {
//        return new UniRetryWithUntil(source, predicate, until);
        return null;
    }
}
