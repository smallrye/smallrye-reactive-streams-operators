package io.smallrye.reactive.streams.api.groups;

import io.smallrye.reactive.streams.api.Uni;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface UniRecoveryGroup<T> {

    /**
     * Produces a new {@link Uni} providing a fallback result when the current {@link Uni} propagates a failure
     * (matching the predicate if any). The fallback value (potentially {@code null})  is used as result by the
     * produced {@link Uni}.
     *
     * @param fallback the fallback value, may be {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> withResult(T fallback);

    /**
     * Produces a new {@link Uni} invoking the given supplier when the current {@link Uni} propagates a failure.
     * (matching the predicate if any). The produced value (potentially {@code null}) is used as result by the produced
     * {@link Uni}. Note that if the supplier throws an exception, the produced {@link Uni} propagates the failure.
     *
     * @param supplier the fallback value, may be {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> withResult(Supplier<T> supplier);

    /**
     * Produces a new {@link Uni} invoking the given function when the current {@link Uni} propagates a failure
     * (matching the predicate if any). The function produces a result (potentially {@code null}) used as result by the
     * produced {@link Uni}. Note that if the fallback function throws an exception, the produced {@link Uni} propagates
     * the failure.
     *
     * @param fallback the fallback function, must not be {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> withResult(Function<? super Throwable, ? extends T> fallback);

    /**
     * Configures a predicate filtering the failures on which this recovery stage will be used.
     * For instance, to recover only when an {@code IOException} is thrown you can use:
     * <code>.recover().fromFailure(IOException.class).withResult("hello")</code>
     * <p>
     * The fallback value ({@code hello}) will only be used if the upstream uni propagate a failure of type
     * {@code IOException}.
     * Note that {@link #fromFailure(Class)} and this method cannot be used together.
     *
     * @param predicate the predicate, must not be {@code null}
     * @return a UniRecoveryGroup configured with the given predicate
     */
    UniRecoveryGroup<T> fromFailure(Predicate<? super Throwable> predicate);

    /**
     * Configures which type of failures are handled this recovery stage will be used.
     * For instance, to recover only when an {@code IOException} is thrown you can use:
     * <code>.recover().fromFailure(IOException.class).withResult("hello")</code>
     * <p>
     * The fallback value ({@code hello}) will only be used if the upstream uni propagate a failure of type
     * {@code IOException}.
     * Note that this method and {@link #fromFailure(Predicate)} cannot be used together.
     *
     * @param type the type of exception, must not be {@code null}
     * @return a UniRecoveryGroup configured with the given type
     */
    <E extends Throwable> UniRecoveryGroup<T> fromFailure(Class<E> type);

    /**
     * Produces a new {@link Uni} providing a fallback {@link Uni} when the current {@link Uni} propagates a failure
     * (matching the predicate if any). The fallback is produced using the given function, and it called when the failure
     * is caught (with the failure as parameter). The produced {@link Uni} is used instead of the current {@link Uni}.
     *
     * @param fallback the function producing the {@link Uni}, must not be {@code null}, must not produce {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> withUni(Function<? super Throwable, ? extends Uni<? extends T>> fallback);

    /**
     * Produces a new {@link Uni} providing a fallback {@link Uni} when the current {@link Uni} propagates a failure
     * (matching the predicate if any). The fallback is produced using the given supplier, and it called when the failure
     * is caught. The produced {@link Uni} is used instead of the current {@link Uni}.
     *
     * @param supplier the fallback supplier, must not be {@code null}, must not produce {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> withUni(Supplier<? extends Uni<? extends T>> supplier);

    /**
     * Produces a new {@link Uni} providing a fallback {@link Uni} when the current {@link Uni} propagates a failure
     * (matching the predicate if any). The fallback {@link Uni} is used instead of the current {@link Uni}.
     *
     * @param fallback the fallback {@link Uni}, must not be {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> withUni(Uni<? extends T> fallback);

    /**
     * Produces an {@link Uni} that retries the resolution of the current {@link Uni}.
     * It re-subscribes to this {@link Uni} if it gets a failure (matching the predicate if any).
     *
     * @return an object to configure the retry
     */
    UniRetryGroup<T> withRetry();


}
