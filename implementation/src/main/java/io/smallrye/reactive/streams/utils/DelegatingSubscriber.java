package io.smallrye.reactive.streams.utils;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Objects;

/**
 * A subscriber delegating to another subscriber and enforcing that parameters are not {@code null}
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DelegatingSubscriber<O> implements Subscriber<O> {
    private final Subscriber<? super O> delegate;

    public DelegatingSubscriber(Subscriber<? super O> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onSubscribe(Subscription s) {
        Objects.requireNonNull(s);
        delegate.onSubscribe(s);
    }

    @Override
    public void onNext(O out) {
        Objects.requireNonNull(out);
        delegate.onNext(out);
    }

    @Override
    public void onError(Throwable t) {
        Objects.requireNonNull(t);
        delegate.onError(t);
    }

    @Override
    public void onComplete() {
        delegate.onComplete();
    }
}
