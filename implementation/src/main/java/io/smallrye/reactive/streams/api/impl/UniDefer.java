package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;

import java.util.function.Supplier;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class UniDefer<T> extends UniOperator<Void, T> {
    private final Supplier<? extends Uni<? extends T>> supplier;

    public UniDefer(Supplier<? extends Uni<? extends T>> supplier) {
        super(null);
        this.supplier = nonNull(supplier, "supplier");
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super T> subscriber) {
        nonNull(subscriber, "subscriber");
        Uni<? extends T> uni;
        try {
            uni = supplier.get();
        } catch (Exception e) {
            subscriber.onSubscribe(EmptySubscription.INSTANCE);
            subscriber.onFailure(e);
            return;
        }

        if (uni == null) {
            subscriber.onSubscribe(EmptySubscription.INSTANCE);
            subscriber.onFailure(new NullPointerException("The supplier produced `null`"));
        } else {
            uni.subscribe().withSubscriber(subscriber);
        }
    }
}
