package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Disposable;
import io.smallrye.reactive.streams.api.UniEmitter;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of the Uni Emitter.
 * This implementation makes sure:
 * <ul>
 *     <li>only the first signal is propagated downstream</li>
 *     <li>cancellation action is called only once and then drop</li>
 * </ul>
 *
 * Implementation Note: This class extends AtomicReference, so do not leak. Also the contained disposable can be:
 * {@code null} (everything is fine), {@code disposed} (we are done), a runnable called on cancellation.
 *
 * @param <T>
 */
public class UniEmitterImpl<T> extends AtomicReference<Disposable> implements UniEmitter<T>, Disposable, UniSubscription {

    private final UniSubscriber<T> downstream;

    UniEmitterImpl(UniSubscriber<T> subscriber) {
        this.downstream = Objects.requireNonNull(subscriber);
    }

    @Override
    public void success(T value) {
        if (get() != DisposableHelper.DISPOSED) { // we are not already disposed
            Disposable previous = getAndSet(DisposableHelper.DISPOSED); // disposed it, as Uni can have a single signal
            if (previous != DisposableHelper.DISPOSED) { // we have been not been disposed in the mean time
                try {
                    downstream.onResult(value);
                } finally {
                    if (previous != null) {
                        previous.dispose(); // Dispose, idempotent operation
                    }
                }
            }
        }
    }

    @Override
    public void fail(Throwable failure) {
        Objects.requireNonNull(failure, "`failure` must not be `null`");
        if (get() != DisposableHelper.DISPOSED) {
            Disposable d = getAndSet(DisposableHelper.DISPOSED);
            if (d != DisposableHelper.DISPOSED) {
                try {
                    downstream.onFailure(failure);
                } finally {
                    if (d != null) {
                        d.dispose();
                    }
                }
            }
        }
    }

    @Override
    public UniEmitter<T> onCancellation(Runnable onCancel) {
        Objects.requireNonNull(onCancel);
        DisposableHelper.set(this, new ActionDisposable(onCancel));
        return this;
    }

    @Override
    public void dispose() {
        DisposableHelper.dispose(this);
    }

    @Override
    public boolean isDisposed() {
        return DisposableHelper.isDisposed(get());
    }

    @Override
    public void cancel() {
        dispose();
    }
}
