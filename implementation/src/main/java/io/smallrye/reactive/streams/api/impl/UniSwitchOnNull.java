package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.function.Supplier;

public class UniSwitchOnNull<I> extends UniOperator<I, I> {
    private final Supplier<Uni<? extends I>> supplier;

    public UniSwitchOnNull(Uni<I> source, Supplier<Uni<? extends I>> supplier) {
        super(source);
        this.supplier = supplier;
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super I> subscriber) {
        UniFlatMap.FlatMapSubscription flatMapSubscription = new UniFlatMap.FlatMapSubscription();
        // Subscribe to the source.
        source().subscribe().withSubscriber(new UniSubscriber<I>() {
            @Override
            public void onSubscribe(UniSubscription subscription) {
                flatMapSubscription.setInitialUpstream(subscription);
                subscriber.onSubscribe(flatMapSubscription);
            }

            @Override
            public void onResult(I result) {
                if (result != null) {
                    subscriber.onResult(result);
                } else {
                    Uni<? extends I> uni;
                    try {
                        uni = supplier.get();
                    } catch (Exception e) {
                        subscriber.onFailure(e);
                        return;
                    }

                    if (uni == null) {
                        subscriber.onFailure(new NullPointerException("The mapper returned `null`"));
                    } else {
                        @SuppressWarnings("unchecked")
                        DelegatingUniSubscriber<? super I> delegate = new DelegatingUniSubscriber(subscriber) {
                            @Override
                            public void onSubscribe(UniSubscription secondSubscription) {
                                flatMapSubscription.replace(secondSubscription);
                            }
                        };
                        uni.subscribe().withSubscriber(delegate);
                    }
                }
            }

            @Override
            public void onFailure(Throwable failure) {
                subscriber.onFailure(failure);
            }
        });
    }
}
