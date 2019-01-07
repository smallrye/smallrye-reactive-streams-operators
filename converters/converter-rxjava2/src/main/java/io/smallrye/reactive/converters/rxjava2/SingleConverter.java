package io.smallrye.reactive.converters.rxjava2;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import org.reactivestreams.Publisher;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

/**
 * Converter handling the RX Java 2 {@link Single} type.
 *
 * <p>
 * <h4>toCompletionStage</h4>
 * The {@link #toCompletionStage(Single)} method returns a {@link CompletionStage} instance completed or failed
 * according to the single emission.
 * </p>
 * <p>
 * <h4>fromCompletionStage</h4>
 * The {@link #fromCompletionStage(CompletionStage)} method returns a {@link Single} instance completed or failed
 * according to the passed {@link CompletionStage} completion. Note that if the future emits a {@code null} value, the
 * {@link Single} emits a failure.
 * </p>
 * <p>
 * <h4>fromPublisher</h4>
 * The {@link #fromPublisher(Publisher)} method returns a {@link Single} emitting the first value of the stream. If the
 * passed {@link Publisher} is empty, the returned {@link Single} fails. If the passed stream emits more than one value,
 * only the first one is used, the other values are discarded.
 * </p>
 * <p>
 * <h4>toRSPublisher</h4>
 * The {@link #toRSPublisher(Single)} method returns a stream emitting a single value followed by the completion signal.
 * If the passed {@link Single} fails, the returned stream also fails.
 * </p>
 */
public class SingleConverter implements ReactiveTypeConverter<Single> {

    @SuppressWarnings("unchecked")
    @Override
    public <T> CompletionStage<T> toCompletionStage(Single instance) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Single<T> s = Objects.requireNonNull(instance);
        //noinspection ResultOfMethodCallIgnored
        s.subscribe(
                future::complete,
                future::completeExceptionally
        );
        return future;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X> Publisher<X> toRSPublisher(Single instance) {
        return instance.toFlowable();
    }

    @Override
    public <X> Single<X> fromCompletionStage(CompletionStage<X> cs) {
        CompletionStage<X> future = Objects.requireNonNull(cs);
        return Single
                .create(emitter ->
                        future.<X>whenComplete((X res, Throwable err) -> {
                            if (!emitter.isDisposed()) {
                                if (err != null) {
                                    emitter.onError(err instanceof CompletionException ? err.getCause() : err);
                                } else {
                                    emitter.onSuccess(res);
                                }
                            }
                        }));
    }

    @Override
    public <X> Single fromPublisher(Publisher<X> publisher) {
        return Flowable.fromPublisher(publisher).firstOrError();
    }

    @Override
    public Class<Single> type() {
        return Single.class;
    }

    @Override
    public boolean emitItems() {
        return true;
    }

    @Override
    public boolean emitAtMostOneItem() {
        return true;
    }

    @Override
    public boolean supportNullValue() {
        return false;
    }

    @Override
    public boolean requireAtLeastOneItem() {
        return true;
    }
}
