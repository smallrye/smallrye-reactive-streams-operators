package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.function.Function;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class UniMapOnResult<I, O> extends UniOperator<I, O> {

    private final Function<? super I, ? extends O> mapper;

    public UniMapOnResult(Uni<I> source, Function<? super I, ? extends O> mapper) {
        super(nonNull(source, "source"));
        this.mapper = nonNull(mapper, "mapper");
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
