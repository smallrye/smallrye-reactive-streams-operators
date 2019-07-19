package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class UniFlatMap<I, O> extends UniOperator<I, O> {


    private final Function<? super I, ? extends Uni<? extends O>> mapper;

    UniFlatMap(Uni<I> source, Function<? super I, ? extends Uni<? extends O>> mapper) {
        super(nonNull(source, "source"));
        this.mapper = nonNull(mapper, "mapper");
    }

    static <I, O> void invokeAndSubstitute(Function<? super I, ? extends Uni<? extends O>> mapper, I input,
                                           WrapperUniSubscriber<? super O> subscriber,
                                           FlatMapSubscription flatMapSubscription) {
        Uni<? extends O> outcome;
        try {
            outcome = mapper.apply(input);
            // We cannot call onResult here, as if onResult would throw an exception
            // it would be caught and onFailure would be called. This would be illegal.
        } catch (Exception e) {
            subscriber.onFailure(e);
            return;
        }

        if (outcome == null) {
            subscriber.onFailure(new NullPointerException("The mapper returned `null`"));
        } else {
            @SuppressWarnings("unchecked")
            DelegatingUniSubscriber<? super O> delegate = new DelegatingUniSubscriber(subscriber) {
                @Override
                public void onSubscribe(UniSubscription secondSubscription) {
                    flatMapSubscription.replace(secondSubscription);
                }
            };

            outcome.subscribe().withSubscriber(delegate);
        }
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super O> subscriber) {
        FlatMapSubscription flatMapSubscription = new FlatMapSubscription();
        // Subscribe to the source.
        source().subscribe().withSubscriber(new UniSubscriber<I>() {
            @Override
            public void onSubscribe(UniSubscription subscription) {
                flatMapSubscription.setInitialUpstream(subscription);
                subscriber.onSubscribe(flatMapSubscription);
            }

            @Override
            public void onResult(I result) {
                invokeAndSubstitute(mapper, result, subscriber, flatMapSubscription);
            }

            @Override
            public void onFailure(Throwable failure) {
                subscriber.onFailure(failure);
            }
        });
    }

    protected static class FlatMapSubscription implements UniSubscription {

        private final AtomicReference<Subscription> upstream = new AtomicReference<>();

        @Override
        public void cancel() {
            Subscription previous = upstream.getAndSet(EmptySubscription.INSTANCE);
            if (previous != null) {
                previous.cancel();
            }
        }

        void setInitialUpstream(Subscription up) {
            if (!upstream.compareAndSet(null, up)) {
                throw new IllegalStateException("Invalid upstream Subscription state, was expected none but got one");
            }
        }

        void replace(Subscription up) {
            Subscription previous = upstream.getAndSet(up);
            if (previous == null) {
                throw new IllegalStateException("Invalid upstream Subscription state, was expected one but got none");
            } else if (previous == EmptySubscription.INSTANCE) {
                // cancelled was called, cancelling up and releasing reference
                upstream.set(null);
                up.cancel();
            }
            // We don't have to cancel the previous subscription as replace is called once the upstream
            // has emitted an item, so it's already disposed.
        }
    }
}
