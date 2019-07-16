package io.smallrye.reactive.streams.api;


import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Subscribes to the {@link Uni} to be notified of the different signals.
 *
 * @param <T> the type of result
 */
public interface UniSubscribe<T> {

    /**
     * Requests the {@link Uni} to start resolving the result.
     * <p>
     * This is a "factory method" and can be called multiple times, each time starting a new {@link UniSubscription}.
     * Each {@link UniSubscription} will work for only a single {@link UniSubscriber}. A {@link UniSubscriber} should
     * only subscribe once to a single {@link Uni}.
     * <p>
     * If the {@link Uni} rejects the subscription attempt or otherwise fails it will signal the failure using
     * {@link UniSubscriber#onFailure(Throwable)}.
     *
     * @param subscriber the subscriber, must not be {@code null}
     * @return the passed subscriber
     */
    UniSubscriber<? super T> withSubscriber(UniSubscriber<? super T> subscriber);

    /**
     * Like {@link #withSubscriber(UniSubscriber)} with creating an artificial {@link UniSubscriber} calling the
     * {@code onResult} and {@code onFailure} callbacks.
     * Unlike {@link #withSubscriber(UniSubscriber)}, this method returns the subscription, and so may await until the
     * subscription is received.
     *
     * @param onResultCallback  callback invoked when the result, potentially {@code null} is received, must not be {@code null}
     * @param onFailureCallback callback invoked when a failure is propagated, must not be {@code null}
     * @return the subscription
     */
    UniSubscription with(Consumer<? super T> onResultCallback, Consumer<? super Throwable> onFailureCallback);

    /**
     * Like {@link #withSubscriber(UniSubscriber)} but provides a {@link CompletableFuture} to retrieve the completed
     * result (potentially {@code null}) and allow chaining operations.
     *
     * @return a {@link CompletableFuture} to retrieve the result and chain operations on the resolved result or
     * failure. The returned {@link CompletableFuture} can also be used to cancel the computation.
     */
    CompletableFuture<T> asCompletionStage();

}
