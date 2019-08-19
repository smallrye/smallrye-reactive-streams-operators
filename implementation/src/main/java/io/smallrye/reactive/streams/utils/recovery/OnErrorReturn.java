package io.smallrye.reactive.streams.utils.recovery;

import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import io.reactivex.Flowable;
import io.reactivex.internal.fuseable.HasUpstreamPublisher;

public class OnErrorReturn<T> extends Flowable<T> implements HasUpstreamPublisher<T> {

    /**
     * The upstream source Publisher.
     */
    private final Flowable<T> source;

    private final Function<? super Throwable, ? extends T> valueSupplier;

    public OnErrorReturn(Flowable<T> source, Function<? super Throwable, ? extends T> valueSupplier) {
        this.source = source;
        this.valueSupplier = valueSupplier;
    }

    @Override
    public final Publisher<T> source() {
        return source;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new OnErrorReturnSubscriber<>(s, valueSupplier));
    }

}
