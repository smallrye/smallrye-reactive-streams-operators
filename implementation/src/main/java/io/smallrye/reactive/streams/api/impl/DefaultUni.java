package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.*;
import org.reactivestreams.Publisher;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

public abstract class DefaultUni<T> implements Uni<T> {


    public abstract void subscribing(WrapperUniSubscriber<? super T> subscriber);


    @Override
    public UniAwaitGroup<T> await() {
        return new UniAwaitGroupImpl<>(this);
    }

    @Override
    public UniSubscribeGroup<T> subscribe() {
        return new UniSubscribeGroupImpl<>(this);
    }

    // Operator
    @Override
    public <O> Uni<O> map(Function<T, O> mapper) {
        return new UniMap<>(this, mapper);
    }

    @Override
    public Uni<T> publishOn(Executor executor) {
        return new OnPublishUniOperator<>(this, executor);
    }

    @Override
    public Uni<T> cache() {
        return new UniCache<>(this);
    }

    // Export

    @Override
    public Publisher<T> toPublisher() {
        // Several important points to note here
        // 1. The subscription on this Uni must be done when we receive a request, not on the subscription
        // 2. The request parameter must be checked to be compliant with Reactive Streams
        // 3. Cancellation can happen 1) before the request (and so the uni subscription); 2) after the request but
        // before the emission; 3) after the emission. In (1) the uni subscription must not happen. In (2), the emission
        // must not happen. In (3), the emission could happen.
        // 4. If the uni result is `null` the stream is completed. If the uni result is not `null`, the stream contains
        // the result and the end of stream signal. In the case of error, the stream propagates the error.
        return subscriber -> {
            AtomicBoolean cancelled = new AtomicBoolean();
            AtomicReference<UniSubscription> upstreamSubscription = new AtomicReference<>();
            UniSubscription downstreamSubscription = new UniSubscription() {
                @Override
                public synchronized void request(long n) {
                    if (n <= 0) {
                        subscriber.onError(new IllegalArgumentException("Invalid request"));
                        return;
                    }

                    if (cancelled.get()) {
                        return;
                    }

                    // We received a request, we subscribe to the uni
                    DefaultUni.this.subscribe().withSubscriber(new UniSubscriber<T>() {
                        @Override
                        public void onSubscribe(UniSubscription subscription) {
                            if (!upstreamSubscription.compareAndSet(null, subscription)) {
                                subscriber.onError(new IllegalStateException("Invalid subscription state - already have a subscription for upstream"));
                            }
                        }

                        @Override
                        public void onResult(T result) {
                            if (!cancelled.get()) {
                                if (result == null) {
                                    subscriber.onComplete();
                                } else {
                                    subscriber.onNext(result);
                                    subscriber.onComplete();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable failure) {
                            if (!cancelled.get()) {
                                subscriber.onError(failure);
                            }
                        }
                    });
                }

                @Override
                public void cancel() {
                    UniSubscription upstream;
                    synchronized (this) {
                        cancelled.set(true);
                        upstream = upstreamSubscription.getAndSet(null);
                    }

                    if (upstream != null) {
                        upstream.cancel();
                    }
                }
            };

            subscriber.onSubscribe(downstreamSubscription);
        };
    }

    @Override
    public Uni<Void> and(Uni<?> other) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <O> O to(Function<? super Uni<T>, O> transformer) {
        return Objects.requireNonNull(transformer, "`transformer` must not be `null`").apply(this);
    }

    @Override
    public <O> O to(Class<O> clazz) {
        return new UniAdaptTo<>(this, Objects.requireNonNull(clazz, "`clazz` must be set")).adapt();
    }

    @Override
    public <O> Uni<O> cast(Class<O> clazz) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <O> Uni<Pair<T, O>> concat(Uni<? extends O> other) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> delay(Duration duration) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> delay(Duration duration, ScheduledExecutorService scheduler) {
        return new UnyDelay<>(this, duration, scheduler);
    }

    @Override
    public Uni<T> filter(Predicate<? super T> filter) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <O> Uni<O> flatMap(Function<? super T, ? extends Uni<? extends O>> transformer) {
        return new UniFlatMap<>(this, transformer);
    }

    @Override
    public Uni<Void> ignore() {
        return this.map(x -> null);
    }

    @Override
    public Uni<T> or(Uni<? extends T> other) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> orElse(T defaultValue) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> orElse(Supplier<T> supplier) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> orElseThrow(Throwable e) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> orElseThrow(Supplier<? extends Throwable> supplier) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> timeout(Duration duration) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> timeout(Duration duration, ScheduledExecutorService executor) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <O> Uni<Pair<? extends T, ? extends O>> zipWith(Uni<? extends O> other) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<Tuple<? extends T>> zipWith(Iterable<Uni<? extends T>> other) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public UniPeekGroup<T> on() {
        return new UniPeekGroupImpl<>(this);
    }


    @Override
    public Uni<T> onFailureMap(Function<? super Throwable, ? extends Throwable> mapper) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> onFailureResume(Function<? super Throwable, ? extends T> mapper) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> onFailureSwitch(Function<? super Throwable, Uni<? extends T>> mapper) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> onFailureReturn(T defaultValue) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> onFailureReturn(Supplier<? extends T> supplier) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> retry() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> retry(int count) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <O> Multi<O> toMulti() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
