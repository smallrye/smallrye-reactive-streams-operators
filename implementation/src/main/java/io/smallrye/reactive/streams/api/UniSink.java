package io.smallrye.reactive.streams.api;


public interface UniSink<T> {

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
    void error(Throwable e);

    // TODO Should we add onCancellation()


}
