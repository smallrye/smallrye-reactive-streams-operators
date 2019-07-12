package io.smallrye.reactive.streams.api;

/**
 * Will receive call to {@link #onSubscribe(UniSubscription)} once after passing an instance of this {@link UniSubscriber}
 * to {@link Uni#subscribe(UniSubscriber)}. Unlike Reactive Streams Subscriber, {@link UniSubscriber} do not request
 * item.
 * <p>
 * After subscription, it can receive:
 * <ul>
 * <li>0 or one invocation of {@link #onResult(Object)}, the passed object can be {@code null}.</li>
 * <li>0 or one invocation of {@link #onFailure​(java.lang.Throwable)} which signals an error state.</li>
 * </ul>
 * <p>
 * Once this subscriber receive a signal, no more signals will be received. So both signals are terminal.
 * <p>
 * Note that unlike in Reactive Streams, the value received in {@link #onResult(Object)} can be {@code null}.
 *
 * @param <T> the expected type of result
 */
public interface UniSubscriber<T> {

    /**
     * Called once the subscribed {@link Uni} has taken into account the subscription. The {@link Uni} would have
     * trigger the computation of the result.
     *
     * @param subscription the subscription allowing to cancel the computation.
     */
    void onSubscribe(UniSubscription subscription);


    /**
     * Called once the result has been computed by the subscribed {@link Uni}.
     *
     * @param result the result, may be {@code null}.
     */
    void onResult(T result);

    /**
     * Called if the computation of the result by the subscriber {@link Uni} failed.
     *
     * @param failure the failure, cannot be {@code null}.
     */
    void onFailure(Throwable failure);

}
