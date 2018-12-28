package io.smallrye.reactive.converters.rxjava2;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import org.reactivestreams.Publisher;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Converter handling the RX Java 2 Completable type.
 *
 * <p>
 * <h4>toCompletionStage</h4>
 * The {@link #toCompletionStage(Completable)} method returns a {@link CompletionStage} instance completed with {@code null} or failed according to
 * the Completable signals.
 * </p>
 * <p>
 * <h4>fromCompletionStage</h4>
 * The {@link #fromCompletionStage(CompletionStage)} method returns a {@link Completable} instance completed or failed according to the
 * passed {@link CompletionStage} completion. If the future emits a {@code null} value, the {@link Completable} is completed
 * successfully. If the future redeems a non-null value, the {@link Completable} completes successfully, but the value is ignored.
 * </p>
 * <p>
 * <h4>fromPublisher</h4>
 * The {@link #fromPublisher(Publisher)} method returns a {@link Completable} emitting the completion signal when the passed stream reached its end.
 * If the passed {@link Publisher} is empty, the returned {@link Completable} completes. If the passed stream emits
 * values, they are discarded. If the passed @{link Publisher} emits a failure before its completion, the returned {@link Completable} fails.
 * </p>
 * <p>
 * <h4>toRSPublisher</h4>
 * The {@link #toRSPublisher(Completable)} method returns a stream emitting an empty stream or a failed stream depending of the {@link Completable}.
 * </p>
 */
public class CompletableConverter implements ReactiveTypeConverter<Completable> {

    @SuppressWarnings("unchecked")
    @Override
    public CompletionStage<Void> toCompletionStage(Completable instance) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Completable s = Objects.requireNonNull(instance);
        //noinspection ResultOfMethodCallIgnored
        s.subscribe(
                () -> future.complete(null),
                future::completeExceptionally
        );
        return future;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Completable fromCompletionStage(CompletionStage cs) {
        CompletionStage<?> future = Objects.requireNonNull(cs);
        return Completable
                .create(emitter ->
                        future.<Void>whenComplete((Object res, Throwable err) -> {
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
    @SuppressWarnings("unchecked")
    public <X> Publisher<X> toRSPublisher(Completable instance) {
        return instance.toFlowable();
    }

    @Override
    public <X> Completable fromPublisher(Publisher<X> publisher) {
        return Flowable.fromPublisher(publisher).ignoreElements();
    }

    @Override
    public Class<Completable> type() {
        return Completable.class;
    }
}
