package io.smallrye.reactive.converters.rxjava1;

import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import org.reactivestreams.Publisher;
import rx.Completable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

/**
 * Converter handling the RX Java 1 {@link Completable} type.
 *
 * <p>
 * <h4>toCompletionStage</h4>
 * The {@link #toCompletionStage(Completable)} method returns a {@link CompletionStage} instance completed with an
 * empty {@link Optional} upon success or failed according to the {@link Completable} signals.
 * </p>
 * <p>
 * <h4>fromCompletionStage</h4>
 * The {@link #fromCompletionStage(CompletionStage)} method returns a {@link Completable} instance completed or failed
 * according to the passed {@link CompletionStage} completion. If the future emits a {@code null} value, the
 * {@link Completable} is completed successfully. If the future redeems a non-null value, the {@link Completable}
 * completes successfully, but the value is ignored. If the future is completed with an exception, the
 * {@link Completable} fails.
 * </p>
 * <p>
 * <h4>fromPublisher</h4>
 * The {@link #fromPublisher(Publisher)} method returns a {@link Completable} emitting the completion signal when the
 * passed stream reached its end. If the passed {@link Publisher} is empty, the returned {@link Completable} completes.
 * If the passed stream emits values, they are discarded. If the passed @{link Publisher} emits a failure before its
 * completion, the returned {@link Completable} fails.
 * </p>
 * <p>
 * <h4>toRSPublisher</h4>
 * The {@link #toRSPublisher(Completable)} method returns a stream emitting an empty stream or a failed stream
 * depending of the {@link Completable}.
 * </p>
 */
public class CompletableConverter implements ReactiveTypeConverter<Completable> {

    @SuppressWarnings("unchecked")
    @Override
    public CompletionStage<Optional<Void>> toCompletionStage(Completable instance) {
        CompletableFuture<Optional<Void>> future = new CompletableFuture<>();
        Completable s = Objects.requireNonNull(instance);
        //noinspection ResultOfMethodCallIgnored
        s.subscribe(
                () -> future.complete(Optional.empty()),
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
                            if (err != null) {
                                if (err instanceof CompletionException) {
                                    emitter.onError(err.getCause());
                                } else {
                                    emitter.onError(err);
                                }
                            } else {
                                emitter.onCompleted();
                            }
                        }));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> Publisher<X> toRSPublisher(Completable instance) {
        return RxJavaInterop.toV2Flowable(instance.toObservable());
    }

    @Override
    public <X> Completable fromPublisher(Publisher<X> publisher) {
        return RxJavaInterop.toV1Observable(publisher).toCompletable();
    }

    @Override
    public Class<Completable> type() {
        return Completable.class;
    }
}
