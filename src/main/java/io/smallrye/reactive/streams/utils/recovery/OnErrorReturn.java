package io.smallrye.reactive.streams.utils.recovery;

import io.reactivex.Flowable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.fuseable.HasUpstreamPublisher;
import io.reactivex.internal.subscribers.SinglePostCompleteSubscriber;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.Objects;
import java.util.function.Function;

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

    static final class OnErrorReturnSubscriber<T>
            extends SinglePostCompleteSubscriber<T, T> {

        final transient Function<? super Throwable, ? extends T> valueSupplier;

        OnErrorReturnSubscriber(Subscriber<? super T> actual, Function<? super Throwable, ? extends T> valueSupplier) {
            super(actual);
            this.valueSupplier = valueSupplier;
        }

        @Override
        public void onNext(T t) {
            produced++;
            actual.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            T v;
            try {
                v = Objects.requireNonNull(valueSupplier.apply(t), "The valueSupplier returned a null value");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                actual.onError(ex);
                return;
            }
            complete(v);
        }

        @Override
        public void onComplete() {
            actual.onComplete();
        }
    }

}
