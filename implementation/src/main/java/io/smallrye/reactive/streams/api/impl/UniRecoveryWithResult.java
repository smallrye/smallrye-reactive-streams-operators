package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class UniRecoveryWithResult<T> extends UniOperator<T, T> {
    private final Function<? super Throwable, ? extends T> fallback;
    private final Predicate<? super Throwable> predicate;

    public UniRecoveryWithResult(Uni<T> source,
                                 Predicate<? super Throwable> predicate,
                                 Function<? super Throwable, ? extends T> fallback) {
        super(Objects.requireNonNull(source, "`source` must not be `null`"));
        this.predicate = predicate;
        this.fallback = Objects.requireNonNull(fallback, "`fallback` must not be `null`");
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super T> subscriber) {
        CallbackUniSubscriber<? super T> inner = new CallbackUniSubscriber<>(
                subscriber::onResult,
                failure -> {
                    if (! passPredicate(predicate, subscriber, failure)) {
                        return;
                    }

                    T substitute;
                    try {
                        substitute = fallback.apply(failure);
                    } catch (Exception e) {
                        subscriber.onFailure(e);
                        return;
                    }

                    subscriber.onResult(substitute);

                });

        subscriber.onSubscribe(inner);
        source().subscribe().withSubscriber(inner);
    }

    static <T> boolean passPredicate(Predicate<? super Throwable> predicate, WrapperUniSubscriber<? super T> subscriber, Throwable failure) {
        if (predicate != null) {
            boolean pass;
            try {
                pass = predicate.test(failure);
            } catch (Exception e) {
                subscriber.onFailure(e);
                return false;
            }
            if (!pass) {
                subscriber.onFailure(failure);
                return false;
            } else {
                // We pass!
                return true;
            }
        } else {
            return true;
        }
    }
}
