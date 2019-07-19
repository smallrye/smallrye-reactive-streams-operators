package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.concurrent.Executor;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class OnPublishUniOperator<I> extends UniOperator<I, I> {
    private final Executor executor;

    OnPublishUniOperator(Uni<I> source, Executor executor) {
        super(source);
        this.executor = nonNull(executor, "executor");
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super I> subscriber) {
        source().subscribe().withSubscriber(new UniSubscriber<I>() {
            @Override
            public void onSubscribe(UniSubscription subscription) {
                subscriber.onSubscribe(new DelegatingUniSubscription(subscription));
            }

            @Override
            public void onResult(I result) {
                executor.execute(() -> subscriber.onResult(result));
            }

            @Override
            public void onFailure(Throwable failure) {
                executor.execute(() -> subscriber.onFailure(failure));
            }
        });
    }
}
