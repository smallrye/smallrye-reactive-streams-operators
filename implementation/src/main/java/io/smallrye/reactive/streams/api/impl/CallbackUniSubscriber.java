package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;
import org.reactivestreams.Subscription;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Implementation of a {@link UniSubscriber} based on callbacks.
 * This implementation also implement {@link UniSubscription} to expose the {@link #cancel()}.
 *
 * @param <T>
 */
final class CallbackUniSubscriber<T> implements UniSubscriber<T>, UniSubscription {

    private final Consumer<? super T> onResultCallback;
    private final Consumer<? super Throwable> onFailureCallback;

    final AtomicReference<UniSubscription> subscription = new AtomicReference<>();

    private static final UniSubscription CANCELLED = () -> {
        // do nothing.
    };

    /**
     * Creates a {@link io.smallrye.reactive.streams.api.UniSubscriber} consuming the result and failure of a
     * {@link io.smallrye.reactive.streams.api.Uni}.
     *
     * @param onResultCallback  callback invoked on result, must not be {@code null}
     * @param onFailureCallback callback invoked on failure, must not be {@code null}
     */
    CallbackUniSubscriber(Consumer<? super T> onResultCallback,
                          Consumer<? super Throwable> onFailureCallback) {
        this.onResultCallback = Objects.requireNonNull(onResultCallback, "`onResultCallback` must not be `null`");
        this.onFailureCallback = Objects.requireNonNull(onFailureCallback, "`onFailureCallback` must not be `null`");
    }

    @Override
    public final void onSubscribe(UniSubscription sub) {
        if (!subscription.compareAndSet(null, sub)) {
            // cancelling this second subscription
            // because we already add a subscription (maybe CANCELLED)
            sub.cancel();
        }
    }

    @Override
    public final void onFailure(Throwable t) {
        UniSubscription sub = subscription.getAndSet(CANCELLED);
        if (sub == CANCELLED) {
            // Already cancelled, do nothing
            return;
        }
        onFailureCallback.accept(t);
    }

    @Override
    public final void onResult(T x) {
        Subscription sub = subscription.getAndSet(CANCELLED);
        if (sub == CANCELLED) {
            // Already cancelled, do nothing
            return;
        }

        try {
            onResultCallback.accept(x);
        } catch (Throwable t) {
            // TODO Log this, or collect the failure
        }
    }

    @Override
    public void cancel() {
        Subscription sub = subscription.getAndSet(CANCELLED);
        if (sub != null) {
            sub.cancel();
        }
    }
}
