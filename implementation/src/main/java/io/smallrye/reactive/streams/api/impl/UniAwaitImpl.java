package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.*;

import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class UniAwaitImpl<T> implements UniAwait<T> {

    private final Uni<T> source;

    public UniAwaitImpl(Uni<T> source) {
        this.source = source;
    }

    @Override
    public T indefinitely() {
        return atMost(null);
    }

    @Override
    public T atMost(Duration timeout) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> reference = new AtomicReference<>();
        AtomicReference<Throwable> referenceToFailure = new AtomicReference<>();
        source.subscribe(new UniSubscriber<T>() {
            @Override
            public void onSubscribe(UniSubscription subscription) {
                // Do nothing.
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
            if (timeout != null) {
                if (!latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                    referenceToFailure.compareAndSet(null, new TimeoutException());
                }
            } else {
                latch.await();
            }
        } catch (InterruptedException e) {
            referenceToFailure.compareAndSet(null, e);
            Thread.currentThread().interrupt();
        }

        Throwable throwable = referenceToFailure.get();
        if (throwable != null) {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            }
            throw new CompletionException(throwable);
        } else {
            return reference.get();
        }
    }

    @Override
    public UniAwaitOptional<T> asOptional() {
        return new UniAwaitOptionalImpl<>(this);
    }
}
