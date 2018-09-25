package io.smallrye.reactive.streams.utils.recovery;

import io.reactivex.FlowableSubscriber;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.subscriptions.EmptySubscription;
import io.reactivex.internal.subscriptions.SubscriptionArbiter;
import io.reactivex.plugins.RxJavaPlugins;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.function.Function;

public class OnErrorResumeWithSubscriber<T> implements FlowableSubscriber<T> {
    private final Subscriber<? super T> actual;
    private final Function<? super Throwable, ? extends Publisher<? extends T>> nextSupplier;
    private final SubscriptionArbiter arbiter;

    private boolean once;

    private boolean done;

    OnErrorResumeWithSubscriber(Subscriber<? super T> actual,
                                Function<? super Throwable, ? extends Publisher<? extends T>> nextSupplier) {
        this.actual = actual;
        this.nextSupplier = nextSupplier;
        this.arbiter = new SubscriptionArbiter();
    }

    @Override
    public void onSubscribe(Subscription s) {
        if (!(s instanceof EmptySubscription)) {
            arbiter.setSubscription(s);
        }
        // else the subscription has already been cancelled, and so we must not subscribe to
        // be compliant with Reactive Streams.
    }

    @Override
    public void onNext(T t) {
        if (done) {
            return;
        }
        actual.onNext(t);
        if (!once) {
            arbiter.produced(1L);
        }
    }

    @Override
    public void onError(Throwable t) {
        if (once) {
            if (done) {
                RxJavaPlugins.onError(t);
            } else {
                actual.onError(t);
            }
            return;
        }
        once = true;

        Publisher<? extends T> p;

        try {
            p = nextSupplier.apply(t);
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            actual.onError(e);
            return;
        }

        if (p == null) {
            NullPointerException npe = new NullPointerException("Publisher is null");
            npe.initCause(t);
            actual.onError(npe);
            return;
        }

        p.subscribe(this);
    }

    @Override
    public void onComplete() {
        if (done) {
            return;
        }
        done = true;
        once = true;
        actual.onComplete();
    }

    Subscription arbiter() {
        return arbiter;
    }
}
