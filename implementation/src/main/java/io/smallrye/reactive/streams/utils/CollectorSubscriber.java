package io.smallrye.reactive.streams.utils;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.internal.subscriptions.DeferredScalarSubscription;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.plugins.RxJavaPlugins;

final class CollectorSubscriber<T, A, R> extends DeferredScalarSubscription<R>
        implements Subscriber<T> {

    private static final long serialVersionUID = 2129956429647866525L;

    private final transient BiConsumer<A, T> accumulator;

    private final transient Function<A, R> finisher;

    private transient A intermediate;

    private transient Subscription subscription;

    private boolean done;

    CollectorSubscriber(Subscriber<? super R> actual,
            A initialValue, BiConsumer<A, T> accumulator, Function<A, R> finisher) {
        super(actual);
        this.intermediate = initialValue;
        this.accumulator = accumulator;
        this.finisher = finisher;
    }

    @Override
    public void onSubscribe(Subscription s) {
        if (SubscriptionHelper.validate(this.subscription, s)) {
            this.subscription = s;

            downstream.onSubscribe(this);

            s.request(Long.MAX_VALUE);
        }
    }

    @Override
    public void onNext(T t) {
        if (!done) {
            try {
                accumulator.accept(intermediate, t);
            } catch (Exception ex) {
                subscription.cancel();
                onError(ex);
            }
        }
    }

    @Override
    public void onError(Throwable t) {
        if (done) {
            RxJavaPlugins.onError(t);
        } else {
            done = true;
            intermediate = null;
            downstream.onError(t);
        }
    }

    @Override
    public void onComplete() {
        if (!done) {
            R r;

            try {
                r = finisher.apply(intermediate);
            } catch (Exception ex) {
                onError(ex);
                return;
            }

            intermediate = null;
            complete(r);
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        subscription.cancel();
    }
}
