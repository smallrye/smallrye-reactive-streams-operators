package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.function.Supplier;

public class UniFailOnNull<T> extends UniOperator<T, T> {
    private final Supplier<? extends Throwable> supplier;

    public UniFailOnNull(Uni<? extends T> source, Supplier<? extends Throwable> supplier) {
        super(source);
        this.supplier = supplier;
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super T> subscriber) {
        source().subscribe().withSubscriber(new UniSubscriber<T>() {
            @Override
            public void onSubscribe(UniSubscription subscription) {
                subscriber.onSubscribe(subscription);
            }

            @Override
            public void onResult(T result) {
                if (result == null) {
                    Throwable failure;
                    try {
                        failure = supplier.get();
                    } catch (Exception e) {
                        subscriber.onFailure(e);
                        return;
                    }
                    if (failure == null) {
                        subscriber.onFailure(new NullPointerException("The supplier produces a `null` value"));
                    } else {
                        subscriber.onFailure(failure);
                    }
                } else {
                    subscriber.onResult(result);
                }
            }

            @Override
            public void onFailure(Throwable failure) {
                subscriber.onFailure(failure);
            }
        });
    }
}
