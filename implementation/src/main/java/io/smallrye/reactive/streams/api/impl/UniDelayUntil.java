package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class UniDelayUntil<T> extends UniOperator<T, T> {
    private final Function<? super T, ? extends Uni<?>> function;
    private final ScheduledExecutorService executor;

    public UniDelayUntil(Uni<T> source, Function<? super T, ? extends Uni<?>> function, ScheduledExecutorService executor) {
        super(source);
        this.function = Objects.requireNonNull(function, "`function` must not be `null`");
        this.executor = executor;
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super T> subscriber) {
        AtomicReference<UniSubscription> reference = new AtomicReference<>();
        AtomicReference<UniSubscription> upstream = new AtomicReference<>();
        source().subscribe().withSubscriber(new UniSubscriber<T>() {
            @Override
            public void onSubscribe(UniSubscription subscription) {
                if (reference.compareAndSet(null, subscription)) {
                    upstream.set(subscription);
                    subscriber.onSubscribe(() -> {
                        UniSubscription old = reference.getAndSet(EmptySubscription.CANCELLED);
                        if (old != null) {
                            old.cancel();
                            upstream.set(null);
                        }
                    });
                }
            }

            @Override
            public void onResult(T result) {
                Uni<?> uni = function.apply(result);
                if (uni == null) {
                    subscriber.onFailure(new NullPointerException("The supplier produces a `null` value"));
                    return;
                }

                if (executor != null) {
                    uni = uni.publishOn(executor);
                }

                uni.subscribe().withSubscriber(new UniSubscriber<Object>() {
                    @Override
                    public void onSubscribe(UniSubscription subscription) {
                        if (reference.compareAndSet(upstream.get(), subscription)) {
                            // clear reference on upstream
                            upstream.set(null);
                        }
                    }

                    @Override
                    public void onResult(Object ignored) {
                        subscriber.onResult(result);
                    }

                    @Override
                    public void onFailure(Throwable failure) {
                        // Propagate this failure
                        subscriber.onFailure(failure);
                    }
                });
            }

            @Override
            public void onFailure(Throwable failure) {
                subscriber.onFailure(failure);
            }
        });
    }
}
