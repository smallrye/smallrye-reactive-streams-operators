package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class UniSubscribeOn<I> extends UniOperator<I, I> {


    private final Executor executor;

    public UniSubscribeOn(Uni<? extends I> source, Executor executor) {
        super(nonNull(source, "source"));
        this.executor = nonNull(executor, "executor");
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super I> subscriber) {
        SubscribeOnUniSubscriber downstream = new SubscribeOnUniSubscriber(subscriber);
        try {
            executor.execute(downstream);
        } catch (Exception e) {
            subscriber.onSubscribe(EmptySubscription.INSTANCE);
            subscriber.onFailure(e);
        }

    }

    class SubscribeOnUniSubscriber implements Runnable, UniSubscriber<I>, UniSubscription {

        final WrapperUniSubscriber<? super I> actual;

        final AtomicReference<UniSubscription> subscription = new AtomicReference<>();

        SubscribeOnUniSubscriber(WrapperUniSubscriber<? super I> actual) {
            this.actual = actual;
        }

        @Override
        public void run() {
            source().subscribe().withSubscriber(this);
        }

        @Override
        public void onSubscribe(UniSubscription s) {
            if (subscription.compareAndSet(null, s)) {
                actual.onSubscribe(this);
            }
        }

        @Override
        public void onResult(I result) {
            actual.onResult(result);
        }

        @Override
        public void onFailure(Throwable failure) {
            actual.onFailure(failure);
        }

        @Override
        public void cancel() {
            UniSubscription upstream = subscription.getAndSet(EmptySubscription.CANCELLED);
            if (upstream != null && upstream != EmptySubscription.CANCELLED) {
                upstream.cancel();
            }
        }
    }
}
