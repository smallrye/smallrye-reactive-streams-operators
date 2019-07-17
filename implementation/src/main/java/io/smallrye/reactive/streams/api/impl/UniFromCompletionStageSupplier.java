package io.smallrye.reactive.streams.api.impl;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public class UniFromCompletionStageSupplier<O> extends UniOperator<Void, O> {
    private final Supplier<? extends  CompletionStage<? extends O>> supplier;

    public UniFromCompletionStageSupplier(Supplier<? extends CompletionStage<? extends O>> supplier) {
        super(null);
        this.supplier = Objects.requireNonNull(supplier, "`supplier` must not be `null`");
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super O> subscriber) {
        CompletionStage<? extends O> stage = supplier.get();

        if (stage == null) {
            subscriber.onSubscribe(EmptySubscription.INSTANCE);
            subscriber.onFailure(new NullPointerException("The produced completion stage is `null`"));
            return;
        }

        forwardFromCompletionStage(stage, subscriber);
    }

    private static <O> void forwardFromCompletionStage(CompletionStage<? extends O> stage, WrapperUniSubscriber<? super O> subscriber) {
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
