package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;

import java.util.Objects;
import java.util.function.Supplier;

public class UniDefer<T> extends UniOperator<Void, T> {
    private final Supplier<? extends Uni<? extends T>> supplier;

    public UniDefer(Supplier<? extends Uni<? extends T>> supplier) {
        super(null);
        this.supplier = Objects.requireNonNull(supplier, "`supplier` cannot be `null`");
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super T> subscriber) {
        Objects.requireNonNull(subscriber, "`subscriber` cannot be `null`");
        Uni<? extends T> uni;
        try {
            uni = supplier.get();
            Objects.requireNonNull(uni, "The supplier must not return `null`");
        } catch (Exception e) {
            subscriber.onSubscribe(EmptySubscription.INSTANCE);
            subscriber.onFailure(e);
            return;
        }
        uni.subscribe(subscriber);
    }
}
