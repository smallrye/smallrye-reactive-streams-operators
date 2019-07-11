package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;

import java.util.Objects;
import java.util.function.Supplier;

public class DeferredUniOperator<T> extends UniOperator<Void, T> {
    private final Supplier<? extends Uni<? extends T>> supplier;

    public DeferredUniOperator(Supplier<? extends Uni<? extends T>> supplier) {
        super(null);
        this.supplier = Objects.requireNonNull(supplier, "`supplier` cannot be `null`");
    }

    @Override
    public void subscribe(UniSubscriber<? super T> subscriber) {
        Objects.requireNonNull(subscriber, "`subscriber` cannot be `null`");
        Uni<? extends T> uni = supplier.get();
        uni.subscribe(subscriber);
    }
}
