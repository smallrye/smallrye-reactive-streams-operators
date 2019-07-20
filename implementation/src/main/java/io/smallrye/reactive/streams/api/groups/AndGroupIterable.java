package io.smallrye.reactive.streams.api.groups;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.impl.UniAnd;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class AndGroupIterable<T1> {

    private final Uni<? extends T1> source;
    private final List<? extends Uni<?>> unis;

    private boolean awaitCompletion;

    public AndGroupIterable(Iterable<? extends Uni<?>> iterable) {
        this(null, iterable, false);
    }

    public AndGroupIterable(Uni<? extends T1> source, Iterable<? extends Uni<?>> iterable) {
        this(source, iterable, false);
    }

    @SuppressWarnings("unchecked")
    public AndGroupIterable(Uni<? extends T1> source, Iterable<? extends Uni<?>> iterable, boolean awaitCompletion) {
        this.source = source;
        List<? extends Uni<?>> others;
        if (iterable instanceof List) {
            others = (List) iterable;
        } else {
            others = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
        }
        this.unis = others;
        this.awaitCompletion = awaitCompletion;
    }


    public AndGroupIterable<T1> awaitCompletion() {
        awaitCompletion = true;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <O> Uni<O> combinedWith(Function<List<?>, O> function) {
        return new UniAnd(source, unis, nonNull(function, "function"), awaitCompletion);
    }

}
