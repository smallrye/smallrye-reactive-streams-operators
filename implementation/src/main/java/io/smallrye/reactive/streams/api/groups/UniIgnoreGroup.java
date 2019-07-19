package io.smallrye.reactive.streams.api.groups;

import io.smallrye.reactive.streams.api.Uni;

import java.util.Objects;
import java.util.function.Supplier;

public class UniIgnoreGroup<T> {

    private final Uni<T> source;

    public UniIgnoreGroup(Uni<T> source) {
        this.source = Objects.requireNonNull(source, "`source` must not be `null`");
    }

    /**
     * Ignores the result of the current {@link Uni} and fails with the passed failure.
     *
     * @param failure the exception to propagate
     * @return the new {@link Uni}
     */
    public Uni<T> andFail(Throwable failure) {
        Objects.requireNonNull(failure, "`failure` must not be `null`");
        return andFail(() -> failure);
    }

    /**
     * Ignores the result of the current {@link Uni}, and fails with a failure produced using the given {@link Supplier}.
     *
     * @param supplier the supplier to produce the failure, must not be {@code null}, must not produce {@code null}
     * @return the new {@link Uni}
     */
    public Uni<T> andFail(Supplier<Throwable> supplier) {
        Objects.requireNonNull(supplier, "`supplier` must not be `null`");
        return source.flatMap(ignored -> Uni.from().failure(supplier));
    }

    /**
     * Like {@link #andFail(Throwable)} but using an {@link Exception}.
     *
     * @return the new {@link Uni}
     */
    public Uni<T> andFail() {
        return andFail(new Exception("Ignored and Failed"));
    }

    /**
     * Ignores the result of the current {@link Uni} and continue with the given {@link Uni}.
     *
     * @param other the uni to continue with, must not be {@code null}
     * @return the new Uni
     */
    public <O> Uni<O> andSwitchTo(Uni<? extends O> other) {
        Objects.requireNonNull(other, "`other` must not be `null`");
        return source.flatMap(ignored -> other);
    }

    /**
     * Ignores the result of the current {@link Uni} and continue with the {@link Uni} produced by the given supplier.
     *
     * @param supplier the supplier to produce the new {@link Uni}, must not be {@code null}, must not produce {@code null}
     * @return the new Uni
     */
    public <O> Uni<O> andSwitchTo(Supplier<Uni<? extends O>> supplier) {
        Objects.requireNonNull(supplier, "`supplier` must not be `null`");
        return source.flatMap(ignored -> supplier.get());
    }

    /**
     * Ignores the result of the current {@link Uni}, and continue with a default value.
     * Note that if the current {@link Uni} fails, the default value is not used.
     *
     * @param fallback the value to continue with, can be {@code null}
     * @return the new {@link Uni}
     */
    public Uni<T> andContinueWith(T fallback) {
        return source.map(ignored -> fallback);
    }

    /**
     * Ignores the result of the current {@link Uni}, and continue with a {@link null} result.
     *
     * @return the new {@link Uni}
     */
    public Uni<Void> andContinueWithNull() {
        return source.map(ignored -> null);
    }

    /**
     * Ignores the result of the current {@link Uni}, and continue with the value produced by the given supplier.
     * Note that if the current {@link Uni} fails, the supplier is not used.
     *
     * @param supplier the default value, must not be {@code null}, can produce {@code null}
     * @return the new {@link Uni}
     */
    public Uni<T> andContinueWith(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "`supplier` must not be `null`");
        return source.map(ignored -> supplier.get());
    }

}
