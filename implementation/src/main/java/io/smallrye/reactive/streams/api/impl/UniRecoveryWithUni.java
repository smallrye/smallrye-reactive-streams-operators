package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.smallrye.reactive.streams.api.impl.UniFlatMap.invokeAndSubstitute;
import static io.smallrye.reactive.streams.api.impl.UniRecoveryWithResult.passPredicate;

public class UniRecoveryWithUni<I> extends UniOperator<I, I> {


    private final Function<? super Throwable, ? extends Uni<? extends I>> fallback;
    private final Predicate<? super Throwable> predicate;

    UniRecoveryWithUni(Uni<I> source, Predicate<? super  Throwable> predicate,
                       Function<? super Throwable, ? extends Uni<? extends I>> fallback) {
        super(Objects.requireNonNull(source, "`source` must not be `null`"));
        this.predicate = predicate;
        this.fallback = Objects.requireNonNull(fallback, "`fallback` must not be `null`");
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
                subscriber.onResult(result);
            }

            @Override
            public void onFailure(Throwable failure) {
                if (! passPredicate(predicate, subscriber, failure)) {
                    return;
                }
                invokeAndSubstitute(fallback, failure, subscriber, flatMapSubscription);
            }
        });
    }


}
