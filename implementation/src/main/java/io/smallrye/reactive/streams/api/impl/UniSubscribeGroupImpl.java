package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.UniSubscribeGroup;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class UniSubscribeGroupImpl<T> implements UniSubscribeGroup<T> {

    private final DefaultUni<T> source;

    public UniSubscribeGroupImpl(DefaultUni<T> source) {
        this.source = Objects.requireNonNull(source, "`source` must not be `null`");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends UniSubscriber<? super T>> S withSubscriber(UniSubscriber<? super T> subscriber) {
        WrapperUniSubscriber.subscribing(source,
                Objects.requireNonNull(subscriber, "`subscriber` must not be `null`"));
        return (S) subscriber;
    }

    @Override
    public UniSubscription with(Consumer<? super T> onResultCallback, Consumer<? super Throwable> onFailureCallback) {
        Objects.requireNonNull(onResultCallback, "`onResultCallback` must not be `null`");
        Objects.requireNonNull(onFailureCallback, "`onFailureCallback` must not be `null`");
        CallbackUniSubscriber<T> subscriber = new CallbackUniSubscriber<>(onResultCallback, onFailureCallback);
        withSubscriber(subscriber);
        return subscriber;
    }

    @Override
    public CompletableFuture<T> asCompletionStage() {
        return UniToCompletionStage.subscribe(source);
    }
}