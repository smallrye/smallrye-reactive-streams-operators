package io.smallrye.reactive.streams.api;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Group of method to configure actions to execute on the various events (subscription, cancellation) and signals (
 * result, failure)
 *
 * @param <T> the type of result
 */
public interface UniPeek<T> {

    /**
     * Produces a new {@link Uni} invoking the given callback when this {@link Uni} resolves a value or propagates
     * a failure.
     *
     * @param callback the callback called with the result (potentially null) or the failure.
     * @return the new {@link Uni}
     */
    Uni<T> terminate(BiConsumer<? super T, Throwable> callback);

    /**
     * Produces a new {@link Uni} invoking the given callback when a subscription to the upstream {@link Uni} is
     * cancelled.
     *
     * @param callback the callback called on cancellation.
     * @return the new {@link Uni}
     */
    Uni<T> cancellation(Runnable callback);

    /**
     * Produces a new {@link Uni} invoking the given callback when this {@link Uni} propagates a failure.
     *
     * @param callback the callback, must not be {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> failure(Consumer<Throwable> callback);

    /**
     * Produces a new {@link Uni} invoking the given callback when the result is resolved (with a value of {@code null}).
     *
     * @param callback the callback, must not be {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> result(Consumer<? super T> callback);

    /**
     * Produces a new {@link Uni} invoking the given callback when a subscriber subscribes to the resulting {@link Uni}.
     *
     * @param callback the callback, must not be {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> subscribe(Consumer<? super UniSubscription> callback);

}
