package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.Objects;
import java.util.function.Function;

public class UniMap<I, O> extends UniOperator<I, O> {

    private final Function<? super I, ? extends O> mapper;

    UniMap(Uni<I> source, Function<I, O> mapper) {
        super(Objects.requireNonNull(source, "`source` must not be `null`"));
        this.mapper = Objects.requireNonNull(mapper, "`mapper` must not be `null`");
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super O> subscriber) {
        source().subscribe().withSubscriber(new UniSubscriber<I>() {
            @Override
            public void onSubscribe(UniSubscription subscription) {
                subscriber.onSubscribe(new DelegatingUniSubscription(subscription));
            }

            @Override
            public void onResult(I result) {
                O outcome;
                try {
                    outcome = mapper.apply(result);
                    // We cannot call onResult here, as if onResult would throw an exception
                    // it would be caught and onFailure would be called. This would be illegal.
                } catch (Exception e) {
                    subscriber.onFailure(e);
                    return;
                }

                subscriber.onResult(outcome);
            }

            @Override
            public void onFailure(Throwable failure) {
                subscriber.onFailure(failure);
            }
        });
    }
}
