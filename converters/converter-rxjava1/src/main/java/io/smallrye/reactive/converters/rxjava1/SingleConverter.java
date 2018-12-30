package io.smallrye.reactive.converters.rxjava1;

import hu.akarnokd.rxjava.interop.RxJavaInterop;
import rx.Single;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import org.reactivestreams.Publisher;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

/**
 * Converter handling the RX Java 1 Single type.
 *
 * <p>
 * <h4>toCompletionStage</h4>
 * The {@link #toCompletionStage(Single)} method returns a {@link CompletionStage} instance completed or failed
 * according to the single emission. Note that if the single emits a {@code null} value, the {@link CompletionStage}
 * completes with an empty {@code Optional}. This behavior differs from the RX Java 2 Single converter, failing in
 * that case (as {@code null} is not a valid value for RX Java 2 {@code Single}).
 * </p>
 * <p>
 * <h4>fromCompletionStage</h4>
 * The {@link #fromCompletionStage(CompletionStage)} method returns a {@link Single} instance completed or failed
 * according to the passed {@link CompletionStage} completion. Note that if the future emits a {@code null} value,
 * the {@link Single} also emits {@code null}. This behavior differs from the RX Java 2 Single converter, failing in
 * that case (as {@code null} is not a valid value for RX Java 2 {@code Single}).
 * </p>
 * <p>
 * <h4>fromPublisher</h4>
 * The {@link #fromPublisher(Publisher)} method returns a {@link Single} emitting the first value of the stream if any.
 * If the passed {@link Publisher} is empty, the returns {@link Single} fails. If the passed stream emits more than one
 * value, only the first one is used, the other values are discarded.
 * </p>
 * <p>
 * <h4>toRSPublisher</h4>
 * The {@link #toRSPublisher(Single)} method returns a stream emitting the values emitted by the passed @{link Single},
 * followed by the completion signal. If the passed {@link Single} fails, the returned stream also fails. If the value
 * emitted by the {@link Single} is {@code null}, the streams fails as {@code null} values are invalid in a
 * {@link Publisher}.
 *
 * </p>
 */
public class SingleConverter implements ReactiveTypeConverter<Single> {

    @SuppressWarnings("unchecked")
    @Override
    public <X> CompletionStage<Optional<X>> toCompletionStage(Single instance) {
        CompletableFuture<Optional<X>> future = new CompletableFuture<>();
        Single<?> s = Objects.requireNonNull(instance);
        //noinspection ResultOfMethodCallIgnored
        s.subscribe(
                v -> future.complete(Optional.ofNullable((X) v)),
                future::completeExceptionally
        );
        return future;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X> Publisher<X> toRSPublisher(Single instance) {
        return RxJavaInterop.toV2Flowable(instance.toObservable());
    }

    @Override
    public <X> Single<X> fromCompletionStage(CompletionStage<X> cs) {
        CompletionStage<X> future = Objects.requireNonNull(cs);
        return Single
                .create(emitter ->
                        future.<X>whenComplete((X res, Throwable err) -> {
                            if (!emitter.isUnsubscribed()) {
                                if (err != null) {
                                    if (err instanceof CompletionException) {
                                        emitter.onError(err.getCause());
                                    } else {
                                        emitter.onError(err);
                                    }
                                } else {
                                    emitter.onSuccess(res);
                                }
                            }
                        }));
    }

    @Override
    public <X> Single fromPublisher(Publisher<X> publisher) {
        return RxJavaInterop.toV1Observable(publisher).first().toSingle();
    }

    @Override
    public Class<Single> type() {
        return Single.class;
    }
}
