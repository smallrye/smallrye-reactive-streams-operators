package io.smallrye.reactive.streams.api.groups;

import io.smallrye.reactive.streams.api.Uni;

import java.util.function.Predicate;

public interface UniRetryGroup<T> {

    /**
     * Produces a {@link Uni} resubscribing to the current {@link Uni} until it gets a result (potentially {@code null})
     * On every failure, it re-subscribes, indefinitely.
     *
     * @return the {@link Uni}
     */
    Uni<T> indefinitely();

    /**
     * Produces a {@link Uni} resubscribing to the current {@link Uni} at most {@code numberOfAttempts} time, until it
     * gets a result (potentially {@code null}). On every failure, it re-subscribes.
     * <p>
     * If the number of attempt is reached, the last failure is propagated.
     *
     * @param numberOfAttempts the number of attempt, must be greater than zero
     * @return a new {@link Uni} retrying at most {@code numberOfAttempts} times to subscribe to the current {@link Uni}
     * until it gets a result. When the number of attempt is reached, the last failure is propagated.
     */
    Uni<T> atMost(long numberOfAttempts);

    // TODO Implement it.
    Uni<T> until(Predicate<? super Throwable> predicate);

    // TODO backOff such as withInitialBackOff(Duration first) withMaxBackOff(Duration max) withJitter(double jitter)

    // TODO add a variant to until taking a Publisher or an Uni. Completion of these indicates that the retry can
    // happen
}
