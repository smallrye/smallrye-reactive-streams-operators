package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.UniSubscriber;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

public class FromCompletionStageUniOperator<O> extends UniOperator<Void, O> {
    private final CompletionStage<O> stage;

    public FromCompletionStageUniOperator(CompletionStage<O> stage) {
        super(null);
        this.stage = Objects.requireNonNull(stage, "`stage` must not be `null`");
    }

    @Override
    public void subscribe(UniSubscriber<? super O> subscriber) {
        AtomicBoolean cancelled = new AtomicBoolean();
        subscriber.onSubscribe(() -> cancelled.set(true));
        stage.whenComplete((res, fail) -> {
            // It's a bit racey here as the cancellation may happen after the check and the subscriber will still
            // be notified
            if (!cancelled.get()) {
                if (fail != null) {
                    subscriber.onFailure(fail);
                } else {
                    subscriber.onResult(res);
                }
            }
        });
    }
}
