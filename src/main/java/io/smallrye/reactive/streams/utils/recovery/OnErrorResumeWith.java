package io.smallrye.reactive.streams.utils.recovery;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.fuseable.HasUpstreamPublisher;
import io.reactivex.internal.subscriptions.SubscriptionArbiter;
import io.reactivex.plugins.RxJavaPlugins;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Objects;
import java.util.function.Function;

public class OnErrorResumeWith<T> extends Flowable<T> implements HasUpstreamPublisher<T> {

    /**
     * The upstream source Publisher.
     */
    private final Flowable<T> source;

    private final Function<? super Throwable, ? extends Publisher<? extends T>> nextSupplier;

    public OnErrorResumeWith(Flowable<T> source,
                             Function<? super Throwable, ? extends Publisher<? extends T>> nextSupplier) {
        this.source = Objects.requireNonNull(source, "source is null");
        this.nextSupplier = nextSupplier;
    }

    @Override
    public final Publisher<T> source() {
        return source;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        OnErrorNextSubscriber<T> parent = new OnErrorNextSubscriber<>(s, nextSupplier);
        s.onSubscribe(parent.arbiter);
        source.subscribe(parent);
    }

    static final class OnErrorNextSubscriber<T> implements FlowableSubscriber<T> {
        final Subscriber<? super T> actual;
        final Function<? super Throwable, ? extends Publisher<? extends T>> nextSupplier;
        final SubscriptionArbiter arbiter;

        boolean once;

        boolean done;

        OnErrorNextSubscriber(Subscriber<? super T> actual, Function<? super Throwable, ? extends Publisher<? extends T>> nextSupplier) {
            this.actual = actual;
            this.nextSupplier = nextSupplier;
            this.arbiter = new SubscriptionArbiter();
        }

        @Override
        public void onSubscribe(Subscription s) {
            arbiter.setSubscription(s);
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
                    return;
                }
                actual.onError(t);
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
    }

}
