package io.smallrye.reactive.streams.api.adapter;

import io.reactivex.Maybe;
import io.reactivex.disposables.Disposable;
import io.smallrye.reactive.streams.api.Uni;

import java.util.concurrent.CompletableFuture;

/**
 * {@link UniAdapter} implementation for the {@link Maybe} type.
 */
public class MaybeUniAdapter implements UniAdapter<Maybe<?>> {
    @Override
    public boolean accept(Class<Maybe<?>> clazz) {
        return Maybe.class.isAssignableFrom(clazz);
    }

    @Override
    public Maybe<?> adaptTo(Uni<?> uni) {
        return Maybe.create(emitter -> {
            CompletableFuture<?> future = uni.subscribe().asCompletionStage();
            emitter.setCancellable(() -> future.cancel(false));
            future.whenComplete((res, fail) -> {
                if (future.isCancelled()) {
                    return;
                }

                if (fail != null) {
                    emitter.onError(fail);
                } else if (res != null) {
                    emitter.onSuccess(res);
                    emitter.onComplete();
                } else {
                    emitter.onComplete();
                }

            });
        });

    }

    @Override
    public Uni<?> adaptFrom(Maybe<?> instance) {
        return Uni.from().emitter(sink -> {
            Disposable disposable = instance.subscribe(
                    sink::success,
                    sink::fail,
                    sink::success
            );

            sink.onCancellation(() -> {
                if (!disposable.isDisposed()) {
                    disposable.dispose();
                }
            });
        });
    }
}
