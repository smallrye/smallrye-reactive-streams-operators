package io.smallrye.reactive.converters.rxjava2;

import io.reactivex.Flowable;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Converter handling the RX Java 2 Flowable type.
 *
 * <p>
 * <h4>toCompletionStage</h4>
 * The {@link #toCompletionStage(Flowable)} method returns a {@link CompletionStage} instance completed or failed according to the
 * observable emissions. The returned {@link CompletionStage} is redeemed with an instance of {@link Optional} to distinguish stream
 * emitting values from empty streams. If the stream is empty, the returned {@link CompletionStage} is completed with an empty optional.
 * If the stream emits multiple values, the first one is used, and the {@link CompletionStage} is completed with an instance of optional
 * wrapping the first emitted item. Other items and potential error are ignored. If the stream fails before emitting a
 * first item, the {@link CompletionStage} is completed with the failure.
 * </p>
 * <p>
 * <h4>fromCompletionStage</h4>
 * The {@link #fromCompletionStage(CompletionStage)} method returns a {@link Flowable} instance completed or failed according to the
 * passed {@link CompletionStage} completion. Note that if the future emits a {@code null} value, the {@link Flowable} completes (empty).
 * If the future completes with a value, the observable emits the value and then completes. If the future completes
 * with a failure, the stream emits the failure.
 * </p>
 * <p>
 * <h4>fromPublisher</h4>
 * The {@link #fromPublisher(Publisher)} method returns n {@link Flowable} emitting the same items, failure and completion as
 * the passed {@link Publisher}. If the passed {@link Publisher} is empty, the returned {@link Flowable} is also empty. This
 * operation is a pass-through for back-pressure and its behavior is determined by the back-pressure behavior
 * of the passed publisher.
 * </p>
 * <p>
 * <h4>toRSPublisher</h4>
 * The {@link #toRSPublisher(Flowable)} method returns a {@link Publisher} emitting the same events as the source {@code publisher}.
 * This operation is a pass-through for back-pressure and its behavior is determined by the back-pressure behavior of
 * the passed publisher.
 * </p>
 */
public class FlowableConverter implements ReactiveTypeConverter<Flowable> {

    @SuppressWarnings("unchecked")
    @Override
    public <T> Publisher<T> toRSPublisher(Flowable instance) {
        return instance;
    }

    @Override
    public Flowable fromPublisher(Publisher publisher) {
        if (publisher instanceof Flowable) {
            return (Flowable) publisher;
        }
        return Flowable.fromPublisher(publisher);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> CompletionStage<T> toCompletionStage(Flowable instance) {
        CompletableFuture<Optional> future = new CompletableFuture<>();
        //noinspection ResultOfMethodCallIgnored
        ((Flowable<?>) instance).firstElement().subscribe(
                res -> future.complete(Optional.of(res)),
                future::completeExceptionally,
                () -> future.complete(Optional.empty())
        );
        return (CompletionStage<T>) future;
    }

    @Override
    public <X> Flowable fromCompletionStage(CompletionStage<X> cs) {
        return Flowable.generate(emitter ->
                ObservableConverter.toStreamEvents(cs, emitter)
        );
    }

    @Override
    public Class<Flowable> type() {
        return Flowable.class;
    }
}
