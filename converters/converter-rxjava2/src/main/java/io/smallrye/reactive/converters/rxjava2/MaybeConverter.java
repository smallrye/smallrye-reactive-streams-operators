package io.smallrye.reactive.converters.rxjava2;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.Single;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import org.reactivestreams.Publisher;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Converter handling the RX Java 2 Maybe type.
 *
 *
 * <strong>Important:</strong> this converter uses the {@link Optional} type to denote whether a result has been emitted or not.
 *
 * <p>
 * <h4>toCompletionStage</h4>
 * The {@link #toCompletionStage(Maybe)} method returns a {@link CompletionStage} instance completed or failed according to the
 * maybe emission. Note that if the maybe emits a {@code null} value, the {@link CompletionStage} fails. If the maybe
 * is empty, the completion stage completes with an empty {@code Optional}. If the maybe emits a value, the completion
 * stage completes with the value wrapped into an {@code Optional}.
 * </p>
 * <p>
 * <h4>fromCompletionStage</h4>
 * The {@link #fromCompletionStage(CompletionStage)} method returns a {@link Maybe} instance completed or failed according to the
 * passed {@link CompletionStage} completion. Note that if the passed future emits a {@code null} value, the {@link Maybe} completes
 * <em>empty</em>.
 * </p>
 * <p>
 * <h4>fromPublisher</h4>
 * The {@link #fromPublisher(Publisher)} method returns a {@link Maybe} emitting the first value of the stream. If the passed
 * {@link Publisher} is empty, the returned {@link Maybe} is empty. If the passed stream emits more than one value, only the first one is
 * used, the other values are discarded.
 * </p>
 * <p>
 * <h4>toRSPublisher</h4>
 * The {@link #toRSPublisher(Maybe)} method returns a stream emitting a single value (if any) followed by the completion signal. If the passed
 * {@link Maybe} fails, the returns streams also fails. If the passed {@link Maybe} is empty, the returned stream is empty.
 * </p>
 */
public class MaybeConverter implements ReactiveTypeConverter<Maybe> {

    @SuppressWarnings("unchecked")
    @Override
    public CompletionStage<Optional> toCompletionStage(Maybe instance) {
        CompletableFuture<Optional> future = new CompletableFuture<>();
        Maybe<?> s = Objects.requireNonNull(instance);
        //noinspection ResultOfMethodCallIgnored
        s.subscribe(
                x -> future.complete(Optional.of(x)),
                future::completeExceptionally,
                () -> future.complete(Optional.empty())
        );
        return future;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Maybe fromCompletionStage(CompletionStage cs) {
        CompletionStage<?> future = Objects.requireNonNull(cs);
        return Maybe
                .create(emitter ->
                        future.<Void>whenComplete((Object res, Throwable err) -> {
                            if (emitter.isDisposed()) {
                                return;
                            }
                            if (err != null) {
                                emitter.onError(err);
                            } else {
                                if (res == null) {
                                    emitter.onComplete();
                                } else if (res instanceof Optional) {
                                    manageOptional(emitter, (Optional) res);
                                } else {
                                    emitter.onSuccess(res);
                                }
                            }

                        }));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private <T> void manageOptional(MaybeEmitter<T> emitter, Optional<T> optional) {
        if (optional.isPresent()) {
            emitter.onSuccess(optional.get());
        } else {
            emitter.onComplete();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X> Publisher<X> toRSPublisher(Maybe instance) {
        return instance.toFlowable();
    }

    @Override
    public <X> Maybe fromPublisher(Publisher<X> publisher) {
        return Flowable.fromPublisher(publisher).firstElement();
    }

    @Override
    public Class<Maybe> type() {
        return Maybe.class;
    }
}
