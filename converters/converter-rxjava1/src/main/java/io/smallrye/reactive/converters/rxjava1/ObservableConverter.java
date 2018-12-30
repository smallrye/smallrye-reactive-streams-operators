package io.smallrye.reactive.converters.rxjava1;


import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import org.reactivestreams.Publisher;
import rx.Emitter;
import rx.Observable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

/**
 * Converter handling the RX Java 1 Observable type.
 *
 * <p>
 * <h4>toCompletionStage</h4>
 * The {@link #toCompletionStage(Observable)} method returns a {@link CompletionStage} instance completed or failed
 * according to the observable emissions.The returned {@link CompletionStage} is redeemed with an instance of
 * {@link Optional}. This {@code Optional} is empty is the {@link Observable} is empty, or emits {@code null} as first
 * value. If the {@link Observable} emits a non-null value, this value is also wrapped into an {@code Optional}
 * instance. If the  {@link Observable} emits multiple values, the first one is used, and the {@link CompletionStage}
 * is completed with an instance of optional wrapping the first emitted item. Other items and potential error are
 * ignored. If the  {@link Observable} fails before emitting a first item, the {@link CompletionStage} is completed
 * with the failure.
 * </p>
 * <p>
 * <h4>fromCompletionStage</h4>
 * The {@link #fromCompletionStage(CompletionStage)} method returns an {@link Observable} instance completed or failed
 * according to the passed {@link CompletionStage} completion. Note that if the future emits a {@code null} value,
 * the {@link Observable} emits the {@code null} value and completes. If the future completes with a value, the
 * observable emits the value and then completes. If the future completes with a failure, the stream emits the failure.
 * </p>
 * <p>
 * <h4>fromPublisher</h4>
 * The {@link #fromPublisher(Publisher)} method returns an {@link Observable} emitting the same items, failure and
 * completion as the passed {@link Publisher}. If the passed {@link Publisher} is empty, the returned {@link Observable}
 * is also empty. The source {@code publisher} is consumed in an unbounded fashion without applying any back-pressure
 * to it.
 * </p>
 * <p>
 * <h4>toRSPublisher</h4>
 * The {@link #toRSPublisher(Observable)} method returns a {@link Publisher} emitting the same events as the source
 * {@code observable}. It converts the  {@link Observable} into a {@link Publisher} but without back-pressure strategy
 * (using a buffer). If the buffer reaches its capacity, an error is sent ({@code MissingBackPressureException}). If
 * the source {@link Observable} emits {@code null} values, the @{code Publisher} fails.
 * </p>
 */
public class ObservableConverter implements ReactiveTypeConverter<Observable> {

    @SuppressWarnings("unchecked")
    @Override
    public <T> Publisher<T> toRSPublisher(Observable instance) {
        return RxJavaInterop.toV2Flowable(instance);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Observable fromPublisher(Publisher publisher) {
        return RxJavaInterop.toV1Observable(publisher);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> CompletionStage<Optional<T>> toCompletionStage(Observable instance) {
        CompletableFuture<Optional<T>> future = new CompletableFuture<>();
        //noinspection ResultOfMethodCallIgnored
        ((Observable<T>) instance)
                .firstOrDefault(null)
                .subscribe(
                    res -> future.complete(Optional.ofNullable(res)),
                    future::completeExceptionally
                );
        return future;
    }

    @Override
    public <X> Observable fromCompletionStage(CompletionStage<X> cs) {
        return Observable.create(emitter -> toStreamEvents(cs, emitter), Emitter.BackpressureMode.ERROR);
    }

    private static <X> void toStreamEvents(CompletionStage<X> cs, Emitter<Object> emitter) {
        cs.whenComplete((X res, Throwable err) -> {
            if (err != null) {
                if (err instanceof CompletionException) {
                    emitter.onError(err.getCause());
                } else {
                    emitter.onError(err);
                }
            } else {
                emitter.onNext(res);
                emitter.onCompleted();
            }
        });
    }

    @Override
    public Class<Observable> type() {
        return Observable.class;
    }
}

