package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class UnyDelay<T> extends UniOperator<T, T> {
    private final Duration delay;
    private final ScheduledExecutorService executor;

    public UnyDelay(Uni<T> source, Duration delay, ScheduledExecutorService executor) {
        super(source);
        this.delay = Objects.requireNonNull(delay, "`delay` must not be `null`");
        this.executor = Objects.requireNonNull(executor, "`executor` must not be `null`");

        if (delay.isNegative() || delay.isZero()) {
            throw new IllegalArgumentException("`delay` must be greater than 0");
        }
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super T> subscriber) {
        AtomicReference<ScheduledFuture<?>> holder = new AtomicReference<>();
        AtomicBoolean done = new AtomicBoolean();
        source().subscribe().withSubscriber(new UniSubscriber<T>() {
            @Override
            public void onSubscribe(UniSubscription subscription) {
                subscriber.onSubscribe(() -> {
                    if (done.compareAndSet(false, true)) {
                        subscription.cancel();
                        // TODO This is a best effort approach, it has race conditions.
                        ScheduledFuture<?> future = holder.get();
                        if (future != null) {
                            future.cancel(true);
                        }
                    }
                });
            }

            @Override
            public void onResult(T result) {
                try {
                    ScheduledFuture<?> future = executor.schedule(() -> subscriber.onResult(result), delay.toMillis(), TimeUnit.MILLISECONDS);
                    holder.set(future);
                } catch (RuntimeException e) {
                    // Typically, a rejected execution exception
                    subscriber.onFailure(e);
                }
            }

            @Override
            public void onFailure(Throwable failure) {
                try {
                    ScheduledFuture<?> future = executor.schedule(() -> subscriber.onFailure(failure), delay.toMillis(), TimeUnit.MILLISECONDS);
                    holder.set(future);
                } catch (RuntimeException e) {
                    // Typically, a rejected execution exception
                    subscriber.onFailure(e);
                }
            }
        });
    }
}
