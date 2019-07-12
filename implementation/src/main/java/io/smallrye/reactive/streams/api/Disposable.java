package io.smallrye.reactive.streams.api;

/**
 * Represents a disposable resource.
 */
public interface Disposable {
    /**
     * Dispose the resource.
     * <p>
     * The operation should be idempotent.
     */
    void dispose();

    /**
     * Check whether this resource has been disposed.
     *
     * @return {@code true} if this resource has been disposed, {@code false} otherwise.
     */
    boolean isDisposed();
}
