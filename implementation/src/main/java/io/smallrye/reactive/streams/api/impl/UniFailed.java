package io.smallrye.reactive.streams.api.impl;

import java.util.Objects;
import java.util.function.Supplier;

public class UniFailed<O> extends UniOperator<Void, O> {
    private final Supplier<? extends Throwable> supplier;

    public UniFailed(Throwable throwable) {
        super(null);
        Objects.requireNonNull(throwable, "`throwable` cannot be `null`");
        this.supplier = () -> throwable;
    }

    public UniFailed(Supplier<? extends Throwable> supplier) {
        super(null);
        Objects.requireNonNull(supplier, "`supplier` cannot be `null`");
        this.supplier = supplier;
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super O> subscriber) {

        Throwable failure;

        try {
            failure = supplier.get();
        } catch (Exception e) {
            subscriber.onSubscribe(EmptySubscription.INSTANCE);
            // Propagate the exception thrown by the supplier
            subscriber.onFailure(e);
            return;
        }

        subscriber.onSubscribe(EmptySubscription.INSTANCE);
        // Propagate the exception produced by the supplier
        subscriber.onFailure(failure);

    }
}
