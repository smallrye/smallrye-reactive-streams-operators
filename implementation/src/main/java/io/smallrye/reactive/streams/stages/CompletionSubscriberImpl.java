package io.smallrye.reactive.streams.stages;

import org.eclipse.microprofile.reactive.streams.CompletionSubscriber;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * Simple implementation of {@link CompletionSubscriber}.
 */
public class CompletionSubscriberImpl<T, R> implements CompletionSubscriber<T, R> {
    private final Subscriber<T> subscriber;
    private final CompletionStage<R> completion;

    public CompletionSubscriberImpl(Subscriber<T> subscriber, CompletionStage<R> completion) {
        this.subscriber = Objects.requireNonNull(subscriber, "Subscriber must not be null");
        this.completion = Objects.requireNonNull(completion, "CompletionStage must not be null");
    }

    public CompletionStage<R> getCompletion() {
        return this.completion;
    }

    public void onSubscribe(Subscription subscription) {
        this.subscriber.onSubscribe(subscription);
    }

    public void onNext(T t) {
        this.subscriber.onNext(t);
    }

    public void onError(Throwable throwable) {
        this.subscriber.onError(throwable);
    }

    public void onComplete() {
        this.subscriber.onComplete();
    }
}
