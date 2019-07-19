package io.smallrye.reactive.streams.api.groups;

import io.reactivex.Emitter;
import io.smallrye.reactive.streams.api.Uni;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface UniFlatMapGroup<T> {

    <R> Uni<R> result(Function<? super T, ? extends Uni<? extends R>> mapper);

    <R> Uni<R> withEmitter(BiConsumer<? super T, Emitter<? extends R>> consumer);
}
