package io.smallrye.reactive.converters;

import io.reactivex.Single;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SingleConverter implements FutureConverter<Single> {

    @Override
    public CompletionStage toCompletionStage(Single instance) {
        CompletableFuture future = new CompletableFuture();
        Single<?> s = Objects.requireNonNull(instance);
        s.subscribe(
                future::complete,
                future::completeExceptionally
        );
        return future;
    }

    @Override
    public Single fromCompletionStage(CompletionStage cs) {
        CompletionStage<?> future = Objects.requireNonNull(cs);
        return Single
                .create(emitter ->
                        future.<Void>whenComplete((res, err) -> {
                            if (!emitter.isDisposed()) {
                                if (err != null) {
                                    emitter.onError(err);
                                } else {
                                    emitter.onSuccess(res);
                                }
                            }
                        }));
    }

    @Override
    public Class<Single> type() {
        return Single.class;
    }
}
