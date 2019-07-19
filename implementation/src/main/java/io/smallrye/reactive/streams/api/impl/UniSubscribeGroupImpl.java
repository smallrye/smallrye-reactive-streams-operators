package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;
import io.smallrye.reactive.streams.api.groups.UniSubscribeGroup;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class UniSubscribeGroupImpl<T> implements UniSubscribeGroup<T> {

    private final DefaultUni<T> source;

    public UniSubscribeGroupImpl(DefaultUni<T> source) {
        this.source = nonNull(source, "source");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends UniSubscriber<? super T>> S withSubscriber(UniSubscriber<? super T> subscriber) {
        WrapperUniSubscriber.subscribing(source,
                nonNull(subscriber, "subscriber"));
        return (S) subscriber;
    }

    @Override
    public UniSubscription with(Consumer<? super T> onResultCallback, Consumer<? super Throwable> onFailureCallback) {
        CallbackUniSubscriber<T> subscriber = new CallbackUniSubscriber<>(
                nonNull(onResultCallback, "onResultCallback"),
                nonNull(onFailureCallback, "onFailureCallback")
        );
        withSubscriber(subscriber);
        return subscriber;
    }

    @Override
    public CompletableFuture<T> asCompletionStage() {
        return UniToCompletionStage.subscribe(source);
    }
}
