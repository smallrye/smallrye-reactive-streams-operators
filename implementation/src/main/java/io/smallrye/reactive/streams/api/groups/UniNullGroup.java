package io.smallrye.reactive.streams.api.groups;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.impl.UniFailOnNull;
import io.smallrye.reactive.streams.api.impl.UniSwitchOnNull;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class UniNullGroup<T> {

    private final Uni<T> source;

    public UniNullGroup(Uni<T> source) {
        this.source = nonNull(source, "source");
    }

    /**
     * If the current {@link Uni} resolves with {@code null}, the produced {@link Uni} propagates the passed failure.
     *
     * @param failure the exception to propagate if the current {@link Uni} is resolved with {@code null}.
     * @return the new {@link Uni}
     */
    public Uni<T> failWith(Throwable failure) {
        nonNull(failure, "failure");
        return failWith(() -> failure);
    }

    /**
     * If the current {@link Uni} resolves with {@code null}, the produced {@link Uni} propagates a failure produced
     * using the given {@link Supplier}.
     *
     * @param supplier the supplier to produce the failure, must not be {@code null}, must not produce {@code null}
     * @return the new {@link Uni}
     */
    public Uni<T> failWith(Supplier<Throwable> supplier) {
        return new UniFailOnNull<>(source, nonNull(supplier, "supplier"));
    }

    /**
     * Like {@link #failWith(Throwable)} but using a {@link NoSuchElementException}.
     *
     * @return the new {@link Uni}
     */
    public Uni<T> fail() {
        return failWith(NoSuchElementException::new);
    }

    public Uni<T> switchTo(Uni<? extends T> other) {
        return switchTo(() -> other);
    }

    public Uni<T> switchTo(Supplier<Uni<? extends T>> supplier) {
        return new UniSwitchOnNull<>(source, nonNull(supplier, "supplier"));
    }

    /**
     * Provides a default value if the current {@link Uni} is completed with {@code null}.
     * Note that if this {@link Uni} fails, the default value is not used.
     *
     * @param fallback the default value, must not be {@code null}
     * @return the new {@link Uni}
     */
    public Uni<T> continueWith(T fallback) {
        return switchTo(Uni.from().value(nonNull(fallback, "fallback")));
    }

    /**
     * Provides a default value if the current {@link Uni} is completed with {@code null}.
     * The value is computed using the given {@link Supplier}.
     * Note that if this {@link Uni} fails, this stage is not used.
     *
     * @param supplier the default value, must not be {@code null}, must not produce {@code null}
     * @return the new {@link Uni}
     */
    public Uni<T> continueWith(Supplier<? extends T> supplier) {
        return switchTo(Uni.from().value(nonNull(supplier, "supplier"))
                .onNull().failWith(NullPointerException::new));
    }

}
