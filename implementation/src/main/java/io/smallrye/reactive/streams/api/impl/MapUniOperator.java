package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.Objects;
import java.util.function.Function;

public class MapUniOperator<I, O> extends UniOperator<I, O> {

    private final Function<? super I, ? extends O> mapper;

    MapUniOperator(Uni<I> source, Function<I, O> mapper) {
        super(Objects.requireNonNull(source, "`source` must not be `null`"));
        this.mapper = Objects.requireNonNull(mapper, "`mapper` must not be `null`");
    }

    @Override
    public void subscribe(UniSubscriber<? super O> subscriber) {
        source().subscribe(new UniSubscriber<I>() {
            @Override
            public void onSubscribe(UniSubscription subscription) {
                subscriber.onSubscribe(new DelegatingUniSubscription(subscription));
            }

            @Override
            public void onResult(I result) {
                try {
                    O outcome = mapper.apply(result);
                    subscriber.onResult(outcome);
                } catch (Exception e) {
                    subscriber.onFailure(e);
                }

            }

            @Override
            public void onFailure(Throwable t) {
                subscriber.onFailure(t);
            }
        });
    }
}
