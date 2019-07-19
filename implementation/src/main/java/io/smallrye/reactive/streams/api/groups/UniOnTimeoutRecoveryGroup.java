package io.smallrye.reactive.streams.api.groups;

import io.smallrye.reactive.streams.api.TimeoutException;
import io.smallrye.reactive.streams.api.Uni;

import java.util.function.Supplier;

public class UniOnTimeoutRecoveryGroup<T> {

    private final UniOnTimeoutGroup<T> timeout;

    public UniOnTimeoutRecoveryGroup(UniOnTimeoutGroup<T> timeout) {
        this.timeout = timeout;
    }

    /**
     * Produces a new {@link Uni} providing a fallback result when the current {@link Uni} propagates a failure
     * (matching the predicate if any). The fallback value (potentially {@code null})  is used as result by the
     * produced {@link Uni}.
     *
     * @param fallback the fallback value, may be {@code null}
     * @return the new {@link Uni}
     */
    public Uni<T> withResult(T fallback) {
        return timeout.fail().recover().fromFailure(TimeoutException.class).withResult(fallback);
    }

    /**
     * Produces a new {@link Uni} invoking the given supplier when the current {@link Uni} times out. The produced
     * value (potentially {@code null}) is used as result by the produced {@link Uni}. Note that if the supplier
     * throws an exception, the produced {@link Uni} propagates the failure.
     *
     * @param supplier the fallback value, may be {@code null}
     * @return the new {@link Uni}
     */
    public Uni<T> withResult(Supplier<T> supplier) {
        return timeout.fail().recover().fromFailure(TimeoutException.class).withResult(supplier);
    }

    /**
     * Produces a new {@link Uni} providing a fallback {@link Uni} when the current {@link Uni} times out. The fallback
     * is produced using the given supplier, and it called when the failure is caught. The produced {@link Uni} is used
     * instead of the current {@link Uni}.
     *
     * @param supplier the fallback supplier, must not be {@code null}, must not produce {@code null}
     * @return the new {@link Uni}
     */
    public Uni<T> withUni(Supplier<? extends Uni<? extends T>> supplier) {
        return timeout.fail().recover().fromFailure(TimeoutException.class).withUni(supplier);
    }

    /**
     * Produces a new {@link Uni} providing a fallback {@link Uni} when the current {@link Uni} times out. The fallback
     * {@link Uni} is used instead of the current {@link Uni}.
     *
     * @param fallback the fallback {@link Uni}, must not be {@code null}
     * @return the new {@link Uni}
     */
    public Uni<T> withUni(Uni<? extends T> fallback) {
        return timeout.fail().recover().fromFailure(TimeoutException.class).withUni(fallback);
    }

    /**
     * Produces an {@link Uni} that retries the resolution of the current {@link Uni} when it times out.
     * It re-subscribes to this {@link Uni} if it gets a failure (matching the predicate if any).
     *
     * @return an object to configure the retry
     */
    public UniRetryGroup<T> withRetry() {
        return timeout.fail().recover().fromFailure(TimeoutException.class).withRetry();
    }


}
