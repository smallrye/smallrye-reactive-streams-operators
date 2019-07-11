package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.Objects;
import java.util.concurrent.Executor;

public class OnPublishUniOperator<I> extends UniOperator<I, I> {
    private final Executor executor;

    OnPublishUniOperator(Uni<I> source, Executor executor) {
        super(source);
        this.executor = Objects.requireNonNull(executor, "`executor` cannot be `null`");
    }

    @Override
    public void subscribe(UniSubscriber<? super I> subscriber) {
        source().subscribe(new UniSubscriber<I>() {
            @Override
            public void onSubscribe(UniSubscription subscription) {
                subscriber.onSubscribe(new DelegatingUniSubscription(subscription));
            }

            @Override
            public void onResult(I result) {
                executor.execute(() -> subscriber.onResult(result));
            }

            @Override
            public void onFailure(Throwable t) {
                executor.execute(() -> subscriber.onFailure(t));
            }
        });
    }
}
