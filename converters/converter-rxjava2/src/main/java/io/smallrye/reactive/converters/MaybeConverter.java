package io.smallrye.reactive.converters;

import io.reactivex.Maybe;
import io.reactivex.Single;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class MaybeConverter implements FutureConverter<Maybe> {

    @Override
    public CompletionStage toCompletionStage(Maybe instance) {
        CompletableFuture future = new CompletableFuture();
        Maybe<?> s = Objects.requireNonNull(instance);
        s.subscribe(
                x -> future.complete(Optional.of(x)),
                future::completeExceptionally,
                () -> future.complete(Optional.empty())
        );
        return future;
    }

    @Override
    public Maybe fromCompletionStage(CompletionStage cs) {
        CompletionStage<?> future = Objects.requireNonNull(cs);
        return Maybe
                .create(emitter ->
                        future.<Void>whenComplete((res, err) -> {
                            if (!emitter.isDisposed()) {
                                if (err != null) {
                                    emitter.onError(err);
                                } else {
                                    if (res == null) {
                                        emitter.onComplete();
                                    } else if (res instanceof Optional) {
                                        if (((Optional) res).isPresent()) {
                                            emitter.onSuccess(((Optional) res).get());
                                        } else {
                                            emitter.onComplete();
                                        }
                                    } else {
                                        emitter.onSuccess(res);
                                    }
                                }
                            }
                        }));
    }

    @Override
    public Class<Maybe> type() {
        return Maybe.class;
    }
}
