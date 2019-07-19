package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.groups.UniRecoveryGroup;
import io.smallrye.reactive.streams.api.groups.UniRetryGroup;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class UniRecoveryGroupImpl<T> implements UniRecoveryGroup<T> {


    private final Uni<T> source;
    private final Predicate<? super Throwable> predicate;

    public UniRecoveryGroupImpl(Uni<T> source, Predicate<? super Throwable> predicate) {
        this.source = nonNull(source, "source");
        this.predicate = predicate;
    }

    @Override
    public Uni<T> withResult(T fallback) {
        return withResult(() -> fallback);
    }

    @Override
    public Uni<T> withResult(Supplier<T> supplier) {
        return withResult(ignored -> supplier.get());
    }

    @Override
    public Uni<T> withResult(Function<? super Throwable, ? extends T> fallback) {
        return new UniRecoveryWithResult<>(source, predicate, fallback);
    }

    @Override
    public UniRecoveryGroup<T> fromFailure(Predicate<? super Throwable> predicate) {
        return new UniRecoveryGroupImpl<>(source, predicate);
    }

    @Override
    public <E extends Throwable> UniRecoveryGroup<T> fromFailure(Class<E> type) {
        nonNull(type, "type");
        return new UniRecoveryGroupImpl<>(source, type::isInstance);
    }

    @Override
    public Uni<T> withUni(Function<? super Throwable, ? extends Uni<? extends T>> fallback) {
        return new UniRecoveryWithUni<>(source, predicate, fallback);
    }

    @Override
    public Uni<T> withUni(Supplier<? extends Uni<? extends T>> supplier) {
        return withUni(ignored -> supplier.get());
    }

    @Override
    public Uni<T> withUni(Uni<? extends T> fallback) {
        return withUni(() -> fallback);
    }

    @Override
    public UniRetryGroup<T> withRetry() {
        return new UniRetryGroupImpl<>(source, predicate);
    }
}
