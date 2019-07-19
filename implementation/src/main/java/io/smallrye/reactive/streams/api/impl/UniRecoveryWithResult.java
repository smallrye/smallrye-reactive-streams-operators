package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;

import java.util.function.Function;
import java.util.function.Predicate;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class UniRecoveryWithResult<T> extends UniOperator<T, T> {
    private final Function<? super Throwable, ? extends T> fallback;
    private final Predicate<? super Throwable> predicate;

    public UniRecoveryWithResult(Uni<T> source,
                                 Predicate<? super Throwable> predicate,
                                 Function<? super Throwable, ? extends T> fallback) {
        super(nonNull(source, "source"));
        this.predicate = predicate;
        this.fallback = nonNull(fallback, "fallback");
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

    @Override
    public void subscribing(WrapperUniSubscriber<? super T> subscriber) {
        CallbackUniSubscriber<? super T> inner = new CallbackUniSubscriber<>(
                subscriber::onResult,
                failure -> {
                    if (!passPredicate(predicate, subscriber, failure)) {
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
}
