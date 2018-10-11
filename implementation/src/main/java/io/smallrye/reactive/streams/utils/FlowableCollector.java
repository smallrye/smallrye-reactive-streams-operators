package io.smallrye.reactive.streams.utils;

import io.reactivex.Flowable;
import io.reactivex.internal.subscriptions.EmptySubscription;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

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
        } catch (Exception ex) {
            source.subscribe(new CancellationSubscriber<>());
            EmptySubscription.error(ex, s);
            return;
        }

        source.subscribe(new CollectorSubscriber<>(s, initialValue, accumulator, finisher));
    }

}
