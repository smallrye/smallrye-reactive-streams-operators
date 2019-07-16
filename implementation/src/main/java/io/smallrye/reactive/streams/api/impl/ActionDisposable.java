package io.smallrye.reactive.streams.api.impl;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A container that implements {@link Disposable} containing a {@link Runnable} instance.
 * The {@link Runnable} is invoked when the disposable is disposed. The contained {@link Runnable} can be {@code null}.
 */
public final class ActionDisposable extends AtomicReference<Runnable> implements Disposable {

    /**
     * Creates a new instance of {@link ActionDisposable}.
     * <p>
     * The constructor inserts the passed value into the container.
     *
     * @param runnable the runnable instance, can be {@code null}
     */
    ActionDisposable(Runnable runnable) {
        super(runnable);
    }

    /**
     * Specialization of {@link Disposable#isDisposed()} just checking if the value is {@code null}
     *
     * @return if the contained value is {@code null}
     */
    @Override
    public boolean isDisposed() {
        return get() == null;
    }

    /**
     * Runs the contained action if any and then removes the reference.
     */
    @Override
    public void dispose() {
        if (get() != null) {
            Runnable runnable = getAndSet(null);
            if (runnable != null) {
                try {
                    runnable.run();
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
