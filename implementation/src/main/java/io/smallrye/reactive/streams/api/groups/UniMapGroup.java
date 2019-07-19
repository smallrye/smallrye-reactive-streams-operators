package io.smallrye.reactive.streams.api.groups;

import io.smallrye.reactive.streams.api.Uni;

import java.util.function.Function;
import java.util.function.Predicate;

public interface UniMapGroup<T> {

    /**
     * Produces a new {@link Uni} invoking the given function when the current {@link Uni} propagates a result. The
     * function can transform the received result into another result. For asynchronous composition, see
     * {@link Uni#flatMap(Function)}.
     *
     * @param mapper the mapper function, must not be {@code null}
     * @return the new {@link Uni}
     */
    <R> Uni<R> result(Function<? super T, ? extends R> mapper);

    /**
     * Produces a new {@link Uni} invoking the given function when the current {@link Uni} propagates a failure. The
     * function can transform the received failure into another exception.
     *
     * @param mapper the mapper function, must not be {@code null}, must not return {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> failure(Function<? super Throwable, ? extends Throwable> mapper);

    /**
     * Produces a new {@link Uni} invoking the given function when the current {@link Uni} propagates a result. The
     * function can transform the received result into a failure. For asynchronous composition, see
     * {@link Uni#flatMap(Function)}.
     *
     * @param mapper the mapper function, must not be {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> toFailure(Function<? super T, ? extends Throwable> mapper);

    /**
     * Casts the item produced by this {@link Uni} to the given type.
     * The returned {@link Uni} fails if the cast fails.
     *
     * @param clazz
     * @param <R>
     * @return
     */
    <R> Uni<R> to(Class<R> clazz);

    /**
     * Returns a new {@link Uni} completed with the {@code true} if the result passes the given predicate,
     * or completed with {@code false} otherwise.
     *
     * @param filter the predicate, must not be {@code null}, may be call with {@code null}
     * @return a {@link Uni} resolving {@code true} if the result of the current {@link Uni} passes the predicate,
     * {@code false} otherwise
     */
    Uni<Boolean> toBoolean(Predicate<? super T> filter);
}
