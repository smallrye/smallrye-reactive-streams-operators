package io.smallrye.reactive.streams.api;

import java.time.Duration;
import java.util.Optional;

/**
 * Likes {@link UniAwait} but wrapping the retrieved result into an {@link Optional}. This optional is empty if the
 * {@link Uni} completes with {@code null}.
 *
 * @param <T> the type of the result
 * @see Uni#await()
 */
public interface UniAwaitOptional<T> {

    /**
     * Subscribes to the {@link Uni} and waits (blocking the caller thread) <strong>indefinitely</strong> until a result
     * or failure is received.
     * <p>
     * If the {@link Uni} is completed with a result, it returns that value, potentially {@code null} if the operation
     * returns {@code null}.
     * If the {@link Uni} is completed with a failure, the original exception is thrown (wrapped in
     * a {@link java.util.concurrent.CompletionException} it's a checked exception).
     * <p>
     * Note that each call to this method triggers a new subscription.
     *
     * @return the result from the {@link Uni} wrapped into an {@link Optional}, empty if the {@link Uni} is resolved
     * with {@code null}
     */
    Optional<T> indefinitely();

    /**
     * Subscribes to the {@link Uni} and waits (blocking the caller thread) <strong>at most</strong> the given duration
     * until a result or failure is received.
     * <p>
     * If the {@link Uni} is completed with a result, it returns that value, potentially {@code null} if the operation
     * returns {@code null}.
     * If the {@link Uni} is completed with a failure, the original exception is thrown (wrapped in
     * a {@link java.util.concurrent.CompletionException} it's a checked exception).
     * If the timeout is reached before completion, a {@link TimeoutException} is thrown.
     * <p>
     * Note that each call to this method triggers a new subscription.
     *
     * @param duration the duration, must not be {@code null}, must not be negative or zero.
     * @return the result from the {@link Uni} wrapped into an {@link Optional}, empty if the {@link Uni} is resolved
     * with {@code null}
     */
    Optional<T> atMost(Duration duration);


}
