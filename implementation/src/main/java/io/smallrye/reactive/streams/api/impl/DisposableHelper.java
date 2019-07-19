package io.smallrye.reactive.streams.api.impl;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility methods for working with {@link Disposable disposables} atomically.
 */
public enum DisposableHelper implements Disposable {
    /**
     * This singleton instance representing a disposed state.
     */
    DISPOSED;

    /**
     * Checks if the given {@link Disposable} is disposed.
     * It checks if the passed reference is equal to the {@link #DISPOSED} enum value.
     *
     * @param disposable the disposable to check
     * @return {@code true} if disposed, {@code false} otherwise
     */
    public static boolean isDisposed(Disposable disposable) {
        return disposable == DISPOSED;
    }

    /**
     * Atomically sets the container and disposes the old contents if any.
     * <p>
     * If the target container contains the common {@link #DISPOSED} instance, the supplied disposable is also disposed.
     *
     * @param container     the target container
     * @param newDisposable the new value to insert in the container
     * @return {@code true} if value previously contained in the container was present and has been disposed,
     * {@code false} otherwise
     */
    public static boolean set(AtomicReference<Disposable> container, Disposable newDisposable) {
        for (; ; ) {
            Disposable current = container.get();
            if (current == DISPOSED) {
                if (newDisposable != null) {
                    newDisposable.dispose();
                }
                return false;
            }
            if (container.compareAndSet(current, newDisposable)) {
                if (current != null) {
                    current.dispose();
                }
                return true;
            }
        }
    }

    /**
     * Atomically disposes the contained @{link Disposable} if not already disposed.
     *
     * @param container the container
     * @return {@code true} if the contained {@link Disposable} got disposed, {@code false} otherwise
     */
    public static boolean dispose(AtomicReference<Disposable> container) {
        Disposable current = container.get();
        Disposable referenceToDisposed = DISPOSED;
        if (current != referenceToDisposed) {
            current = container.getAndSet(referenceToDisposed);
            if (current != referenceToDisposed) {
                if (current != null) {
                    current.dispose();
                }
                return true;
            }
        }
        return false;
    }

    // Disposed instance implementation

    @Override
    public void dispose() {
        // ignore me
    }

    @Override
    public boolean isDisposed() {
        return true;
    }
}
