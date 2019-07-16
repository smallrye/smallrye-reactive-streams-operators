package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;
import org.reactivestreams.Subscription;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class UniToCompletionStage {

    public static <T> CompletableFuture<T> subscribe(Uni<T> uni) {
        final AtomicReference<Subscription> ref = new AtomicReference<>();

        CompletableFuture<T> future = new CompletableFuture<T>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean cancelled = super.cancel(mayInterruptIfRunning);
                if (cancelled) {
                    Subscription s = ref.getAndSet(null);
                    if (s != null) {
                        s.cancel();
                    }
                }
                return cancelled;
            }
        };

       uni.subscribe().withSubscriber(new UniSubscriber<T>() {
            @Override
            public void onSubscribe(UniSubscription subscription) {
                if (! ref.compareAndSet(null, subscription)) {
                    future.completeExceptionally(new IllegalStateException("Invalid subscription state - Already having an upstream subscription"));
                }
            }

            @Override
            public void onResult(T result) {
                if (ref.getAndSet(null) != null) {
                    future.complete(result);
                }
            }

            @Override
            public void onFailure(Throwable failure) {
                if (ref.getAndSet(null) != null) {
                    future.completeExceptionally(failure);
                }
            }
        });
        return future;
    }

}
