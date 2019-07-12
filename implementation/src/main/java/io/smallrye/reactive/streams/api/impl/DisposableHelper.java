package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Disposable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility methods for working with {@link io.smallrye.reactive.streams.api.Disposable disposables} atomically.
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
        for (; ;) {
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
     * Atomically sets the content of the container to the given {@link Disposable}.
     * Returns {@code true} if the previous content was {@code null}, {@code false} otherwise.
     * <p>
     * If the container contains the common {@link #DISPOSED} instance, the supplied disposable is disposed.
     * If the container contains other non-null {@link Disposable}, an {@link IllegalStateException} is thrown
     *
     * @param container     the container
     * @param newDisposable the disposable to set, must not be {@code null}
     * @return {@code true} on success as described above, {@code false} otherwise.
     */
    public static boolean setIfEmpty(AtomicReference<Disposable> container, Disposable newDisposable) {
        Objects.requireNonNull(newDisposable, "`newDisposable` must not be `null`");
        if (!container.compareAndSet(null, newDisposable)) {
            newDisposable.dispose();
            if (container.get() != DISPOSED) {
                throw new IllegalStateException("The container value was already disposed");
            }
            return false;
        }
        return true;
    }

    /**
     * Atomically replaces the Disposable in the container with the given new Disposable but does not dispose the
     * previous one.
     *
     * @param container     the container
     * @param newDisposable the new disposable, can be {@code null}
     * @return {@code false} if the container contained {@link #DISPOSED}, {@code true} otherwise.
     */
    public static boolean replace(AtomicReference<Disposable> container, Disposable newDisposable) {
        for (; ; ) {
            Disposable current = container.get();
            if (current == DISPOSED) {
                if (newDisposable != null) {
                    newDisposable.dispose();
                }
                return false;
            }
            if (container.compareAndSet(current, newDisposable)) {
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

    /**
     * Checks that {@code current} is {@code null} and {@code next} is not {@code null}.
     * If {@code current} is not {@code null}, it will be disposed.
     *
     * @param current the current Disposable
     * @param next    the next Disposable
     * @return {@code true} if current == null and next != null
     */
    public static boolean validate(Disposable current, Disposable next) {
        if (next == null) {
            return false;
        }
        if (current != null) {
            // Something went wrong somewhere...
            next.dispose();
            return false;
        }
        return true;
    }

    /**
     * Atomically tries to insert the given {@link Disposable} into the container if it is {@code null} or disposes it
     * if the container contains {@link #DISPOSED}.
     *
     * @param container     the  container
     * @param newDisposable the disposable to insert
     * @return {@code true} if successful, {@code false otherwise}
     */
    public static boolean trySet(AtomicReference<Disposable> container, Disposable newDisposable) {
        if (!container.compareAndSet(null, newDisposable)) {
            if (container.get() == DISPOSED) {
                newDisposable.dispose();
            }
            return false;
        }
        return true;
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
