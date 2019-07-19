package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static io.smallrye.reactive.streams.api.impl.UniRecoveryWithResult.passPredicate;

public class UniRetryWithAttempts<T> extends UniOperator<T, T> {
    private final Predicate<? super Throwable> predicate;
    private final long maxAttempts;

    public UniRetryWithAttempts(Uni<T> source, Predicate<? super Throwable> predicate, long maxAttempts) {
        super(source);
        this.predicate = predicate;
        this.maxAttempts = maxAttempts;
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("`maxAttempts` must be greater than 0");
        }
    }


    @Override
    public void subscribing(WrapperUniSubscriber<? super T> subscriber) {
        AtomicInteger numberOfSubscriptions = new AtomicInteger(0);
        UniSubscriber<T> retryingSubscriber = new UniSubscriber<T>() {
            AtomicReference<UniSubscription> reference = new AtomicReference<>();

            @Override
            public void onSubscribe(UniSubscription subscription) {
                if (numberOfSubscriptions.getAndIncrement() == 0) {
                    subscriber.onSubscribe(() -> {
                        UniSubscription old = reference.getAndSet(EmptySubscription.CANCELLED);
                        if (old != null) {
                            old.cancel();
                        }
                    });
                } else {
                    reference.compareAndSet(null, subscription);
                }
            }

            @Override
            public void onResult(T result) {
                if (reference.get() != EmptySubscription.CANCELLED) {
                    subscriber.onResult(result);
                }
            }

            @Override
            public void onFailure(Throwable failure) {
                if (reference.get() != EmptySubscription.CANCELLED) {
                    if (!passPredicate(predicate, subscriber, failure)) {
                        return;
                    }

                    if (numberOfSubscriptions.get() > maxAttempts) {
                        subscriber.onFailure(failure);
                        return;
                    }

                    // retry.
                    UniSubscription old = reference.getAndSet(null);
                    if (old != null) {
                        old.cancel();
                    }
                    resubscribe(source(), this);
                }
            }
        };

        source().subscribe().withSubscriber(retryingSubscriber);
    }

    private void resubscribe(Uni<? extends T> upstream, UniSubscriber<T> subscriber) {
        upstream.subscribe().withSubscriber(subscriber);
    }
}
