package io.smallrye.reactive.streams.api.groups;

import io.smallrye.reactive.streams.api.Uni;

import java.util.Arrays;
import java.util.List;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class UniOrGroup<T> {

    private final Uni<T> source;

    public UniOrGroup(Uni<T> source) {
        this.source = nonNull(source, "source");
    }

    public Uni<T> uni(Uni<T> other) {
        return unis(source, other);
    }

    public Uni<T> unis(Uni<T>... other) {
        List<Uni<T>> list = Arrays.asList(other);
        return Uni.any(list);
    }

}
