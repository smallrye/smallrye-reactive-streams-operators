package io.smallrye.reactive.converters.rxjava2;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Converter handling the RX Java 2 Observable type.
 *
 * <p>
 * <h4>toCompletionStage</h4>
 * The {@link #toCompletionStage(Observable)} method returns a {@link CompletionStage} instance completed or failed according to the
 * observable emissions. The returned {@link CompletionStage} is redeemed with an instance of {@link Optional} to distinguish stream
 * emitting values from empty streams. If the stream is empty, the returned {@link CompletionStage} is completed with an empty optional.
 * If the stream emits multiple values, the first one is used, and the {@link CompletionStage} is completed with an instance of optional
 * wrapping the first emitted item. Other items and potential error are ignored. If the stream fails before emitting a
 * first item, the {@link CompletionStage} is completed with the failure.
 * </p>
 * <p>
 * <h4>fromCompletionStage</h4>
 * The {@link #fromCompletionStage(CompletionStage)} method returns an {@link Observable} instance completed or failed according to the
 * passed {@link CompletionStage} completion. Note that if the future emits a {@code null} value, the {@link Observable} completes (empty).
 * If the future completes with a value, the observable emits the value and then completes. If the future completes
 * with a failure, the stream emits the failure.
 * </p>
 * <p>
 * <h4>fromPublisher</h4>
 * The {@link #fromPublisher(Publisher)} method returns an {@link Observable} emitting the same items, failure and completion as
 * the passed {@link Publisher}. If the passed {@link Publisher} is empty, the returned {@link Observable} is also empty. The
 * source {@code publisher} is consumed in an unbounded fashion without applying any back-pressure to it.
 * </p>
 * <p>
 * <h4>toRSPublisher</h4>
 * The {@link #toRSPublisher(Observable)} method returns a {@link Publisher} emitting the same events as the source {@code observable}.
 * It converts the  {@link Observable} into a {@link Publisher} but without back-pressure strategy (using a buffer).
 * If the buffer reaches its capacity, an error is sent ({@code MissingBackPressureException}).
 * </p>
 */
public class ObservableConverter implements ReactiveTypeConverter<Observable> {

    @SuppressWarnings("unchecked")
    @Override
    public <T> Publisher<T> toRSPublisher(Observable instance) {
        return instance.toFlowable(BackpressureStrategy.MISSING);
    }

    @Override
    public Observable fromPublisher(Publisher publisher) {
        return Observable.fromPublisher(publisher);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> CompletionStage<T> toCompletionStage(Observable instance) {
        CompletableFuture<Optional> future = new CompletableFuture<>();
        //noinspection ResultOfMethodCallIgnored
        ((Observable<?>) instance).firstElement().subscribe(
                res -> future.complete(Optional.of(res)),
                future::completeExceptionally,
                () -> future.complete(Optional.empty())
        );
        return (CompletionStage<T>) future;
    }

    @Override
    public <X> Observable fromCompletionStage(CompletionStage<X> cs) {
        return Observable.generate(emitter -> toStreamEvents(cs, emitter));

    }

    static <X> void toStreamEvents(CompletionStage<X> cs, Emitter<Object> emitter) {
        cs.whenComplete((X res, Throwable err) -> {
            if (res != null) {
                emitter.onNext(res);
                emitter.onComplete();
            } else {
                if (err != null) {
                    emitter.onError(err);
                } else {
                    emitter.onComplete();
                }
            }
        });
    }

    @Override
    public Class<Observable> type() {
        return Observable.class;
    }
}
