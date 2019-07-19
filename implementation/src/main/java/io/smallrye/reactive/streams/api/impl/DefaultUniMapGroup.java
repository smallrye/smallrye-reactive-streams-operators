package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.groups.UniMapGroup;

import java.util.function.Function;
import java.util.function.Predicate;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class DefaultUniMapGroup<T> implements UniMapGroup<T> {

    private final Uni<T> source;

    public DefaultUniMapGroup(Uni<T> source) {
        this.source = nonNull(source, "source");
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
        nonNull(mapper, "mapper");
        return source.flatMap(t -> Uni.from().failure(mapper.apply(t)));
    }

    @Override
    public <R> Uni<R> to(Class<R> clazz) {
        nonNull(clazz, "clazz");
        return result(clazz::cast);
    }

    @Override
    public Uni<Boolean> toBoolean(Predicate<? super T> filter) {
        nonNull(filter, "filter");
        return result(filter::test);
    }


}
