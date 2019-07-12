package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.*;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

public abstract class UniImpl<T> implements Uni<T> {

    @Override
    public void subscribe(UniSubscriber<? super T> subscriber) {
        WrapperUniSubscriber.subscribing(this, subscriber);
    }

    public abstract void subscribing(WrapperUniSubscriber<? super T> subscriber);

    @Override
    public CompletableFuture<T> subscribeToCompletionStage() {
        final AtomicReference<Subscription> ref = new AtomicReference<>();

        CompletableFuture<T> future = new CompletableFuture<T>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean cancelled = super.cancel(mayInterruptIfRunning);
                if (cancelled) {
                    Subscription s = ref.getAndSet(null);
                    if (s != null) {
                        s.cancel();
                    }
                }
                return cancelled;
            }
        };

        subscribe(new UniSubscriber<T>() {
            @Override
            public void onSubscribe(UniSubscription subscription) {
                if (! ref.compareAndSet(null, subscription)) {
                    future.completeExceptionally(new IllegalStateException("Invalid subscription state - Already having an upstream subscription"));
                }
            }

            @Override
            public void onResult(T result) {
                if (ref.getAndSet(null) != null) {
                    future.complete(result);
                }
            }

            @Override
            public void onFailure(Throwable failure) {
                if (ref.getAndSet(null) != null) {
                    future.completeExceptionally(failure);
                }
            }
        });
        return future;
    }

    @Override
    public T block() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> reference = new AtomicReference<>();
        AtomicReference<Throwable> referenceToFailure = new AtomicReference<>();
        subscribe(new UniSubscriber<T>() {
            @Override
            public void onSubscribe(UniSubscription subscription) {

            }

            @Override
            public void onResult(T result) {
                reference.compareAndSet(null, result);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable failure) {
                referenceToFailure.compareAndSet(null, failure);
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            referenceToFailure.compareAndSet(null, e);
            Thread.currentThread().interrupt();
        }

        Throwable throwable = referenceToFailure.get();
        if (throwable != null) {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            }
            throw new RuntimeException(throwable);
        } else {
            return reference.get();
        }

    }

    @Override
    public Optional<T> blockOptional() {
        return Optional.ofNullable(block());
    }

    // Operator
    @Override
    public <O> Uni<O> map(Function<T, O> mapper) {
        return new MapUniOperator<>(this, mapper);
    }

    @Override
    public Uni<T> publishOn(Executor executor) {
        return new OnPublishUniOperator<>(this, executor);
    }

    @Override
    public Uni<T> cache() {
        return new CacheUniOperator<>(this);
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
                    UniImpl.this.subscribe(new UniSubscriber<T>() {
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
    public T block(Duration timeout) throws TimeoutException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<Void> and(Uni<?> other) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <O> O to(Function<? super Uni<T>, O> transformer) {
        throw new UnsupportedOperationException("Not implemented yet");
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
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> filter(Predicate<? super T> filter) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <O> Uni<O> flatMap(Function<? super T, ? extends Uni<? extends O>> transformer) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<Void> ignore() {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public Uni<T> doOnResult(Consumer<T> consumer) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> doOnSubscribe(Consumer<? super UniSubscription> consumer) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> doOnFailure(Consumer<Throwable> consumer) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> doFinally(BiConsumer<T, Throwable> consumer) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> onErrorMap(Function<? super Throwable, ? extends Throwable> mapper) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> onErrorResume(Function<? super Throwable, ? extends T> mapper) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> onErrorSwitch(Function<? super Throwable, Uni<? extends T>> mapper) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> onErrorReturn(T defaultValue) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Uni<T> onErrorReturn(Supplier<? extends T> supplier) {
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
}
