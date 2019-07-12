package io.smallrye.reactive.streams.api;


/**
 * An object allowing to send signals to the downstream {@link Uni}.
 * {@link Uni} propagates a single signals, once the first is propagated, the others signals have no effect.
 *
 * @param <T> the expected type of item.
 */
public interface UniEmitter<T> {

    /**
     * Completes with the given (potentially {@code null}) value.
     * <p>
     * Calling this method multiple times or after the other
     * terminating methods has no effect.
     *
     * @param value the value
     */
    void success(T value);

    /**
     * Completes without any value.
     * <p>
     * Calling this method multiple times or after the
     * other terminating methods has no effect.
     */
    default void success() {
        success(null);
    }

    /**
     * Fails with the given exception
     * <p>
     * Calling this method multiple times or after the other terminating methods is
     * an unsupported operation.
     *
     * @param e the exception
     */
    void fail(Throwable e);

    /**
     * Attaches a cancellation action invoked when the downstream uni subscription is cancelled.
     * This method allow propagating the cancellation to the source and potentially cleanup resources.
     *
     * @param onCancel the action to run on cancellation, must not be {@code null}
     * @return this emitter
     */
    UniEmitter<T> onCancellation(Runnable onCancel);


}
