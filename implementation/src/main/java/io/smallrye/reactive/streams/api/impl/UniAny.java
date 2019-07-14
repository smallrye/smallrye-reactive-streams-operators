package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class UniAny<T> extends UniOperator<Void, T> {

    private final List<Uni<? super T>> challengers;

    public UniAny(Iterable<? extends Uni<? super T>> iterable) {
        super(null);
        Objects.requireNonNull(iterable, "`iterable` must not be `null`");
        this.challengers = new ArrayList<>();
        iterable.forEach(u -> challengers.add(Objects.requireNonNull(u, "`iterable` must not contain a `null` value")));
    }

    public UniAny(Uni<? super T>[] array) {
        super(null);
        Objects.requireNonNull(array, "`array` must not be `null`");
        this.challengers = new ArrayList<>();
        for (Uni<? super T> u : array) {
            challengers.add(Objects.requireNonNull(u, "`array` must not contain a `null` value"));
        }
        ;
    }


    @SuppressWarnings("unchecked")
    @Override
    public void subscribing(WrapperUniSubscriber<? super T> subscriber) {
        if (challengers.isEmpty()) {
            // Empty
            subscriber.onSubscribe(EmptySubscription.INSTANCE);
            subscriber.onResult(null);
            return;
        }

        if (challengers.size() == 1) {
            // Just subscribe to the first and unique uni.
            Uni<? super T> uni = challengers.get(0);
            uni.subscribe((UniSubscriber) subscriber);
            return;
        }

        // Barrier - once set to {@code true} the signals are ignored
        AtomicBoolean completedOrCancelled = new AtomicBoolean();

        List<CompletableFuture<? super T>> futures = new ArrayList<>();
        challengers.forEach(uni -> {
            CompletableFuture<? super T> future = uni.subscribeToCompletionStage();
            futures.add(future);
        });

        // Do not call unSubscribe until we get all the futures.
        // But at the same time we can't start resolving as we didn't give a subscription to the subscriber
        // yet.

        subscriber.onSubscribe(() -> {
            if (completedOrCancelled.compareAndSet(false, true)) {
                // Cancel all
                futures.forEach(cf -> cf.cancel(false));
            }
        });

        // Once the subscription has been given, start resolving
        futures.forEach(future ->
                future.whenComplete((res, fail) -> {
                    if (completedOrCancelled.compareAndSet(false, true)) {
                        // Cancel other
                        futures.forEach(cf -> {
                            if (cf != future) {
                                cf.cancel(false);
                            }
                        });
                        if (fail != null) {
                            subscriber.onFailure(fail);
                        } else {
                            subscriber.onResult((T) res);
                        }
                    }
                })
        );
    }
}
