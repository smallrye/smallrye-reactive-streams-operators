package io.smallrye.reactive.streams.api.impl;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

public class UniFromCompletionStage<O> extends UniOperator<Void, O> {
    private final CompletionStage<O> stage;

    public UniFromCompletionStage(CompletionStage<O> stage) {
        super(null);
        this.stage = Objects.requireNonNull(stage, "`stage` must not be `null`");
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super O> subscriber) {
        subscriber.onSubscribe(() -> stage.toCompletableFuture().cancel(false));
        stage.whenComplete((res, fail) -> {
            if (fail != null) {
                subscriber.onFailure(fail);
            } else {
                subscriber.onResult(res);
            }
        });
    }
}
