package io.smallrye.reactive.converters;

import io.reactivex.Completable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CompletableConverter implements FutureConverter<Completable> {

    @Override
    public CompletionStage toCompletionStage(Completable instance) {
        CompletableFuture future = new CompletableFuture();
        Completable s = Objects.requireNonNull(instance);
        s.subscribe(
                () -> future.complete(null),
                future::completeExceptionally
        );
        return future;
    }

    @Override
    public Completable fromCompletionStage(CompletionStage cs) {
        CompletionStage<Void> future = Objects.requireNonNull(cs);
        return Completable
                .create(emitter ->
                        future.<Void>whenComplete((res, err) -> {
                            if (!emitter.isDisposed()) {
                                if (err != null) {
                                    emitter.onError(err);
                                } else {
                                    emitter.onComplete();
                                }
                            }
                        }));
    }

    @Override
    public Class<Completable> type() {
        return Completable.class;
    }
}
