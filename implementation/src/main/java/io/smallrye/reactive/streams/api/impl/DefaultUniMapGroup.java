package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.groups.UniMapGroup;
import io.smallrye.reactive.streams.api.impl.UniMapOnFailure;
import io.smallrye.reactive.streams.api.impl.UniMapOnResult;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class DefaultUniMapGroup<T> implements UniMapGroup<T> {

    private final Uni<T> source;

    public DefaultUniMapGroup(Uni<T> source) {
        this.source = Objects.requireNonNull(source, "`source` must not be `null`");
    }

    @Override
    public <R> Uni<R> result(Function<? super T, ? extends R> mapper) {
        return new UniMapOnResult<>(source, mapper);
    }

    @Override
    public Uni<T> failure(Function<? super Throwable, ? extends Throwable> mapper) {
        return new UniMapOnFailure<>(source, mapper);
    }

    @Override
    public Uni<T> toFailure(Function<? super T, ? extends Throwable> mapper) {
        Objects.requireNonNull(mapper, "`mapper` must not be `null`");
        return source.flatMap(t -> Uni.from().failure(mapper.apply(t)));
    }

    @Override
    public <R> Uni<R> to(Class<R> clazz) {
        Objects.requireNonNull(clazz, "`clazz` must not be `null`");
        return result(clazz::cast);
    }

    @Override
    public Uni<Boolean> toBoolean(Predicate<? super T> filter) {
        Objects.requireNonNull(filter, "`filter` must not be `null`");
        return result(filter::test);
    }


}
