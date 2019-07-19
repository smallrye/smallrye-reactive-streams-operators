package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.time.Duration;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;
import static io.smallrye.reactive.streams.api.impl.ParameterValidation.validate;

public class UniFailOnTimeout<I> extends UniOperator<I, I> {
    private final Duration timeout;
    private final Supplier<Throwable> supplier;
    private final ScheduledExecutorService executor; // TODO must be a parameter

    public UniFailOnTimeout(Uni<I> source, Duration timeout, Supplier<Throwable> supplier, ScheduledExecutorService executor) {
        super(nonNull(source, "source"));
        this.timeout = validate(timeout, "onTimeout");
        this.supplier = nonNull(supplier, "supplier");
        this.executor = executor == null ? Infrastructure.getDefaultExecutor() : executor;
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super I> subscriber) {
        AtomicBoolean doneOrCancelled = new AtomicBoolean();
        AtomicReference<ScheduledFuture<?>> task = new AtomicReference<>();

        source().subscribe().withSubscriber(new UniSubscriber<I>() {
            @Override
            public void onSubscribe(UniSubscription subscription) {
                // Configure the watch dog at subscription time.
                try {
                    task.set(executor.schedule(() -> {
                        if (doneOrCancelled.compareAndSet(false, true)) {
                            sendTimeout(subscriber);
                        }
                    }, timeout.toMillis(), TimeUnit.MILLISECONDS));
                } catch (RejectedExecutionException e) {
                    // Executor out of service.
                    subscriber.onFailure(e);
                    return;
                }

                subscriber.onSubscribe(() -> {
                    if (doneOrCancelled.compareAndSet(false, true)) {
                        // cancelling
                        subscription.cancel();
                        ScheduledFuture<?> future = task.getAndSet(null);
                        if (future != null) {
                            future.cancel(false);
                        }
                    }
                });
            }

            @Override
            public void onResult(I result) {
                if (doneOrCancelled.compareAndSet(false, true)) {
                    ScheduledFuture<?> future = task.getAndSet(null);
                    if (future != null) {
                        future.cancel(false);
                    }
                    subscriber.onResult(result);
                }
            }

            @Override
            public void onFailure(Throwable failure) {
                if (doneOrCancelled.compareAndSet(false, true)) {
                    ScheduledFuture<?> future = task.getAndSet(null);
                    if (future != null) {
                        future.cancel(false);
                    }
                    subscriber.onFailure(failure);
                }
            }
        });
    }

    private void sendTimeout(WrapperUniSubscriber<? super I> subscriber) {
        Throwable throwable;
        try {
            throwable = supplier.get();
        } catch (Exception e) {
            subscriber.onFailure(e);
            return;
        }
        if (throwable == null) {
            subscriber.onFailure(new NullPointerException("`supplier` produced a `null` value"));
        } else {
            subscriber.onFailure(throwable);
        }
    }
}
