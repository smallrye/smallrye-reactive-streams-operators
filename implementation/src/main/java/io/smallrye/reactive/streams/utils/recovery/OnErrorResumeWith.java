package io.smallrye.reactive.streams.utils.recovery;

import java.util.Objects;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import io.reactivex.Flowable;
import io.reactivex.internal.fuseable.HasUpstreamPublisher;

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
        OnErrorResumeWithSubscriber<T> parent = new OnErrorResumeWithSubscriber<>(s, nextSupplier);
        s.onSubscribe(parent.arbiter());
        source.subscribe(parent);
    }

}
