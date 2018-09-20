package io.smallrye.reactive.streams.utils;

import io.reactivex.Flowable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.subscriptions.DeferredScalarSubscription;
import io.reactivex.internal.subscriptions.EmptySubscription;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.plugins.RxJavaPlugins;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * Collect elements of the upstream with the help of the Collector'subscription callback functions.
 *
 * @param <T> the upstream value type
 * @param <A> the accumulated type
 * @param <R> the result type
 */
public final class FlowableCollector<T, A, R> extends Flowable<R> {

    private final Publisher<T> source;

    private final Collector<T, A, R> collector;

    public FlowableCollector(Publisher<T> source, Collector<T, A, R> collector) {
        this.source = source;
        this.collector = collector;
    }

    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        A initialValue;
        BiConsumer<A, T> accumulator;
        Function<A, R> finisher;

        try {
            initialValue = collector.supplier().get();
            accumulator = collector.accumulator();
            finisher = collector.finisher();
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            source.subscribe(new CancellationSubscriber<>());
            EmptySubscription.error(ex, s);
            return;
        }

        source.subscribe(new CollectorSubscriber<>(s, initialValue, accumulator, finisher));
    }

    static final class CollectorSubscriber<T, A, R> extends DeferredScalarSubscription<R>
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

                actual.onSubscribe(this);

                s.request(Long.MAX_VALUE);
            }
        }

        @Override
        public void onNext(T t) {
            if (!done) {
                try {
                    accumulator.accept(intermediate, t);
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
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
                actual.onError(t);
            }
        }

        @Override
        public void onComplete() {
            if (!done) {
                R r;

                try {
                    r = finisher.apply(intermediate);
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
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
}
