package io.smallrye.reactive.streams.api.impl;

import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FromPublisherUniOperator<O> extends UniOperator<Void, O> {
    private final PublisherBuilder<O> publisher;

    public FromPublisherUniOperator(PublisherBuilder<O> publisher) {
        super(null);
        this.publisher = Objects.requireNonNull(publisher, "`stage` must not be `null`");
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super O> subscriber) {
        CompletableFuture<Optional<O>> stage = publisher.findFirst().run().toCompletableFuture();
        subscriber.onSubscribe(() -> stage.cancel(false));
        stage.whenComplete((res, fail) -> {
            if (fail != null) {
                subscriber.onFailure(fail);
            } else {
                // If we get the end of stream signal, inject `null`.
                subscriber.onResult(res.orElse(null));
            }
        });
    }
}
