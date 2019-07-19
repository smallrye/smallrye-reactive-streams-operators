package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.function.Function;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class UniMapOnFailure<I> extends UniOperator<I, I> {

    private final Function<? super Throwable, ? extends Throwable> mapper;

    public UniMapOnFailure(Uni<I> source, Function<? super Throwable, ? extends Throwable> mapper) {
        super(nonNull(source, "source"));
        this.mapper = nonNull(mapper, "mapper");
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super I> subscriber) {
        source().subscribe().withSubscriber(new UniSubscriber<I>() {
            @Override
            public void onSubscribe(UniSubscription subscription) {
                subscriber.onSubscribe(new DelegatingUniSubscription(subscription));
            }

            @Override
            public void onResult(I result) {
                subscriber.onResult(result);
            }

            @Override
            public void onFailure(Throwable failure) {
                Throwable outcome;
                try {
                    outcome = mapper.apply(failure);
                    // We cannot call onFailure here, as if onFailure would throw an exception
                    // it would be caught and onFailure would be called again. This would be illegal.
                } catch (Exception e) {
                    subscriber.onFailure(e);
                    return;
                }
                if (outcome == null) {
                    subscriber.onFailure(new NullPointerException("The mapper returned a `null` value"));
                } else {
                    subscriber.onFailure(outcome);
                }
            }
        });
    }
}
