package io.smallrye.reactive.streams.api;

import io.smallrye.reactive.streams.api.impl.*;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * A {@link Uni} represent a lazy asynchronous action. Once triggered, by a {@link UniSubscriber}, it starts computing
 * and emits the result or failures.
 * <p>
 * The {@link Uni} type proposes a set of operators to chain operations.
 *
 * @param <T> the type of item produced by the {@link Uni}
 */
public interface Uni<T> {

    /**
     * Creates a new {@link Uni} that completes immediately after being subscribed to with the specified (potentially
     * {@code null}) value.
     *
     * @param value the value, can be {@code null}
     * @param <T>   the type of the produced item
     * @return the new {@link Uni}
     */
    static <T> Uni<T> of(T value) {
        return new OfUniOperator<>(value);
    }

    /**
     * Creates a new {@link Uni} that completes immediately after being subscribed to the specified value if
     * {@link Optional#isPresent()} or {@code null} otherwise.
     *
     * @param value the optional, must not be {@code null}
     * @param <T>   the type of the produced item
     * @return the new {@link Uni}
     */
    static <T> Uni<T> fromOptional(Optional<T> value) {
        return new OfUniOperator<>(value);
    }

    /**
     * Creates a {@link Uni} that emits the passed failure immediately after being subscribed to.
     *
     * @param failure the failure, must not be {@code null}
     * @param <T>     the type of the {@link Uni}, must be explicitly set as in {@code Uni.<String>failed(exception);}
     * @return the new {@link Uni}
     */
    static <T> Uni<T> failed(Throwable failure) {
        return new FailedUni<>(Objects.requireNonNull(failure, "The passed exception must not be `null`"));
    }

    /**
     * Creates a {@link Uni} that emits a failure produced using the passed supplier immediately after being subscribed
     * to.
     *
     * @param supplier the supplier producing the failure, must not be {@code null}
     * @param <T>      the type of the {@link Uni}, must be explicitly set as in
     *                 {@code Uni.<String>failed(() -> exception);}
     * @return the new {@link Uni}
     */
    static <T> Uni<T> failed(Supplier<? extends Throwable> supplier) {
        Objects.requireNonNull(supplier, "The supplier must not be `null`");
        return new FailedUni<>(supplier);
    }

    // TODO Javadoc
    static <T> Uni<T> fromCompletionStage(CompletionStage<T> stage) {
        return new FromCompletionStageUni<>(Objects.requireNonNull(stage, "The passed completion stage must not be `null`"));
    }

    // TODO Javadoc
    static <T> Uni<T> fromPublisher(Publisher<T> publisher) {
        return fromPublisher(ReactiveStreams.fromPublisher(publisher));
    }

    static <T> Uni<T> fromPublisher(PublisherBuilder<T> publisher) {
        return new FromPublisherUniOperator<>(Objects.requireNonNull(publisher, "The passed publisher stage must not be `null`"));
    }

    static <T> Uni<T> defer(Supplier<? extends Uni<? extends T>> supplier) {
        return new DeferredUni<>(Objects.requireNonNull(supplier, "The passed supplier must not be `null`"));
    }

    /**
     * Create a {@link Uni} deferring the logic to the given consumer. The consumer is called at subscription time.
     *
     * @param consumer
     * @param <T>
     * @return
     */
    static <T> Uni<T> create(Consumer<UniEmitter<T>> consumer) {
       return new UniCreate(consumer);
    }

    static Uni<Void> empty() {
        return of(null);
    }

    static <T> Uni<T> never() {
        return new FromCompletionStageUni<>(new CompletableFuture<>());
    }

    /**
     * Returns a new {@link Uni} with the first result or failure emitted by the set of <em>competing</em> {@link Uni}.
     *
     * @param iterable
     * @param <T>
     * @return
     */
    static <T> Uni<T> any(Iterable<? extends Uni<? super T>> iterable) {
        throw new UnsupportedOperationException("To be implemented");
    }


    /**
     * Requests the {@link Uni} to start resolving the result.
     * <p>
     * This is a "factory method" and can be called multiple times, each time starting a new {@link UniSubscription}.
     * Each {@link UniSubscription} will work for only a single {@link UniSubscriber}. A {@link UniSubscriber} should
     * only subscribe once to a single {@link Uni}.
     * <p>
     * If the {@link Uni} rejects the subscription attempt or otherwise fails it will signal the failure using
     * {@link UniSubscriber#onFailure(Throwable)}.
     *
     * @param subscriber the subscriber, must not be {@code null}
     */
    void subscribe(UniSubscriber<? super T> subscriber);

    /**
     * Like {@link #subscribe(UniSubscriber)} but provides a {@link CompletableFuture} to retrieve the completed result
     * (potentially {@code null}) and allow chaining operations.
     *
     * @return a {@link CompletableFuture} to retrieve the result and chain operations on the resolved result or
     * failure. The returned {@link CompletableFuture} can also be used to cancel the computation.
     */
    CompletableFuture<T> subscribeToCompletionStage();


    /**
     * Subscribes to this {@link Uni} and wait (blocking the caller thread) indefinitely until a result or failure is
     * received.
     * <p>
     * On success, it returns that value, potentially {@code null} if the operation returns {@code null}.
     * On error,  the original exception is thrown (wrapped in a {@link java.util.concurrent.CompletionException} if
     * it was a checked exception).
     * <p>
     * Note that each call to {@link #block()} triggers a new subscription.
     *
     * @return the result
     */
    T block();

    /**
     * Like {@link #block()} but throws a {@link java.util.concurrent.TimeoutException} if the passed timeout is reached.
     *
     * @param timeout
     * @return
     * @throws TimeoutException
     */
    T block(Duration timeout) throws TimeoutException;


    /**
     * Subscribes to this {@link Uni} and wait (blocking the caller thread) indefinitely until a result or failure is
     * received.
     * <p>
     * On success, it returns an {@link Optional} containing the value, empty if the operation returns {@code null}.
     * On error,  the original exception is thrown (wrapped in a {@link java.util.concurrent.CompletionException} if
     * it was a checked exception).
     * <p>
     * Note that each call to {@link #blockOptional()} triggers a new subscription.
     *
     * @return an optional wrapping the result.
     */
    Optional<T> blockOptional();

    // Operators

    /**
     * Transforms the result (potentially null) emitted by this {@link Uni} by applying a (synchronous) function to it.
     * For asynchronous composition, look at flatMap.
     *
     * @param mapper the mapper function, must not be {@code null}
     * @param <O>    the output type
     * @return a new {@link Uni} computing a result of type {@code <O>}.
     */
    <O> Uni<O> map(Function<T, O> mapper);

    /**
     * Runs {@link UniSubscriber#onResult(Object)}  and {@link UniSubscriber#onFailure(Throwable)} on the supplied
     * {@link Executor}.
     * <p>
     * This operator influences the threading context where the rest of the operators in the downstream chain it will
     * execute, up to a new occurrence of {@code publishOn(Executor)}.
     *
     * @param executor the executor to use, must not be {@code null}
     * @return a new {@link Uni}
     */
    Uni<T> publishOn(Executor executor);

    /**
     * Caches the completion (result or failure) of this {@link Uni} and replays it for all further {@link UniSubscriber}.
     *
     * @return the new {@link Uni}. Unlike regular {@link Uni}, re-subscribing to this {@link Uni} does not re-compute
     * the outcome but replayed the cached signals.
     */
    Uni<T> cache();

    /**
     * Joins the completion of this {@link Uni} and another {@link Uni} into the resulting {@link Uni}
     *
     * @param other
     * @return
     */
    Uni<Void> and(Uni<?> other);

    /**
     * Transforms this {@link Uni} into an instance of {@code O} using the given {@code transformer} function.
     *
     * @param transformer
     * @param <O>
     */
    <O> O to(Function<? super Uni<T>, O> transformer);

    /**
     * Transforms this {@link Uni} into an instance of the given class. The transformations acts as follows:
     * <ol>
     * <li>If this is an instance of O - return this</li>
     * <li>If there is on the classpath, an implementation of {@link io.smallrye.reactive.streams.api.adapter.UniAdapter}
     * for the type O, the adapter is used</li>
     * <li>If O has a {@code fromPublisher} method, this method is called with a {@link Publisher} produced
     * using {@link #toPublisher()}</li>
     * <li>If O has a {@code from} method, this method is called with a {@link Publisher} produced
     *  using {@link #toPublisher()}</li>
     * </ol>
     *
     * @param clazz the output class
     * @param <O>   the produced type
     * @return an instance of O
     * @throws RuntimeException if the transformation fails.
     */
    <O> O to(Class<O> clazz);


    static <T, I> Uni<T> from(I instance) {
        return UniAdaptFrom.adaptFrom(instance);
    }

    /**
     * Casts the item produced by this {@link Uni} to the given type.
     * The returned {@link Uni} fails if the cast fails.
     *
     * @param clazz
     * @param <O>
     * @return
     */
    <O> Uni<O> cast(Class<O> clazz);

    /**
     * Concatenates the result of this {@link Uni} with the result from the passed {@link Uni}.
     * If this or the other {@link Uni} fails, the resulting {@link Uni} propagates the failure.
     *
     * @param other
     * @param <O>
     * @return
     */
    <O> Uni<Pair<T, O>> concat(Uni<? extends O> other);

    /**
     * Delays the completion of this {@link Uni} by the given duration.
     * The downstream signals are sent on the default executor.
     *
     * @param duration
     * @return
     */
    Uni<T> delay(Duration duration);

    /**
     * Delays the completion of this {@link Uni} by the given duration.
     * The downstream signals are sent on the passed executor.
     *
     * @param duration
     * @param scheduler
     * @return
     */
    Uni<T> delay(Duration duration, ScheduledExecutorService scheduler);

    /**
     * Returns a new {@link Uni} completed with the value resolved by this {@link Uni} if it passes the check, or
     * completed with {@code null} if not.
     *
     * @param filter
     * @return
     */
    Uni<T> filter(Predicate<? super T> filter);

    /**
     * Transform the result resolved by this {@link Uni} asynchronously, returning the value emitted the the produced
     * mapper function.
     *
     * @param mapper
     * @param <O>
     * @return
     */
    <O> Uni<O> flatMap(Function<? super T, ? extends Uni<? extends O>> mapper);

    /**
     * @return a {@link Uni} propagating the same signal as this {@link Uni}. If this {@link Uni} resolves with a
     * {@code non-null} value, the returned {@link Uni} produces {@code null}.
     */
    Uni<Void> ignore();

    /**
     * Produces a new {@link Uni} propagating the first signals emitted by either this {@link Uni} or the other {@link Uni}.
     *
     * @param other
     * @return
     */
    Uni<T> or(Uni<? extends T> other);

    /**
     * Provides a default value if this {@link Uni} is completed with {@code null}.
     * Note that if this {@link Uni} fails, the default value is not used.
     *
     * @param defaultValue the default value
     * @return
     */
    Uni<T> orElse(T defaultValue);

    /**
     * Provides a default value if this {@link Uni} is completed with {@code null}.
     *
     * @param supplier
     * @return
     */
    Uni<T> orElse(Supplier<T> supplier);

    /**
     * If this {@link Uni} resolves with {@code null}, the produced {@link Uni} throws the passed failure.
     *
     * @param e the exception to propagate if this {@link Uni} is resolved with {@code null}.
     * @return the new {@link Uni}
     */
    Uni<T> orElseThrow(Throwable e);

    /**
     * If this {@link Uni} resolves with {@code null}, the produced {@link Uni} throws the exception returned
     * by the passed supplier.
     *
     * @param supplier the producer of the exception to propagate if this {@link Uni} is resolved with {@code null}.
     * @return the new {@link Uni}
     */
    Uni<T> orElseThrow(Supplier<? extends Throwable> supplier);

    /**
     * Produces a {@link Uni} sending a {@link TimeoutException} if this {@link Uni} does not resolve successfully (with
     * a value or {@code null}) before the passed duration.
     * The downstream processing are going to be called on the default executor.
     *
     * @param duration the duration
     * @return the new {@link Uni}
     */
    Uni<T> timeout(Duration duration);

    /**
     * Produces a {@link Uni} sending a {@link TimeoutException} if this {@link Uni} does not resolve successfully (with
     * a value or {@code null}) before the passed duration.
     * <p>
     * The downstream processing are going to be called on the passed executor.
     *
     * @param duration the duration
     * @param executor the executor to use, {@code null} to use the default executor
     * @return the new {@link Uni}
     */
    Uni<T> timeout(Duration duration, ScheduledExecutorService executor);

    /**
     * Aggregates two given {@link Uni Unis} into a new {@link Uni} that will be fulfilled when both {@link Uni} are
     * resolved successfully aggregating their values into a {@link Pair}.
     * <p>
     * The produced {@link Uni} forwards the failure if one of the two {@link Uni Unis} produces a failure. This will
     * cause the other {@link Uni} to be cancelled.
     *
     * @param left  the first participant
     * @param right the second participant
     * @param <L>   the type produced by the first participant
     * @param <R>   the type produced by the second participant
     * @return the new {@link Uni}
     */
    static <L, R> Uni<Pair<L, R>> zip(Uni<? extends L> left, Uni<? extends R> right) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Aggregates the given {@link Uni Unis} into a new {@link Uni} that will be fulfilled when <strong>all</strong>
     * {@link Uni Unis} are resolved successfully aggregating their values into a {@link Tuple}.
     * <p>
     * The produced {@link Uni} forwards the failure if one of the {@link Uni Unis} produces a failure. This will cause
     * the other {@link Uni} to be cancelled.
     *
     * @param iterable the set of participants
     * @param <O>      the type produced by the participants
     * @return the new {@link Uni}
     */
    static <O> Uni<Tuple<O>> zip(Iterable<Uni<? extends O>> iterable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Aggregates this {@link Uni} with another one. It results into a new {@link Uni} that will be fulfilled when both
     * {@link Uni} are resolved successfully aggregating their values into a {@link Pair}.
     * <p>
     * The produced {@link Uni} forwards the failure if one of the two {@link Uni Unis} produces a failure. This will
     * cause the other {@link Uni} to be cancelled.
     *
     * @param other the other participant
     * @param <O>   the type produced by the second participant
     * @return the new {@link Uni}
     */
    <O> Uni<Pair<? extends T, ? extends O>> zipWith(Uni<? extends O> other);

    /**
     * Aggregates the given {@link Uni Unis} into a new {@link Uni} that will be fulfilled when <strong>all</strong>
     * {@link Uni Unis} are resolved successfully aggregating their values into a {@link Tuple}.
     * <p>
     * The produced {@link Uni} forwards the failure if one of the {@link Uni Unis} produces a failure. This will
     * cause the other {@link Uni} to be cancelled.
     *
     * @param iterable the other participants
     * @return the new {@link Uni}
     */
    Uni<Tuple<? extends T>> zipWith(Iterable<Uni<? extends T>> iterable);

    // Actions

    /**
     * Produces a new {@link Uni} invoking the given consumer when the result is resolved (with a value of {@code null}).
     *
     * @param consumer the consumer, must not be {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> doOnResult(Consumer<T> consumer);

    /**
     * Produces a new {@link Uni} invoking the given consumer when a subscriber subscribes to the resulting {@link Uni}.
     *
     * @param consumer the consumer, must not be {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> doOnSubscribe(Consumer<? super UniSubscription> consumer);

    /**
     * Produces a new {@link Uni} invoking the given consumer when this {@link Uni} propagates a failure.
     *
     * @param consumer the consumer, must not be {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> doOnFailure(Consumer<Throwable> consumer);

    /**
     * Produces a new {@link Uni} invoking the given bi-consumer when this {@link Uni} resolves a value or propagates
     * a failure.
     *
     * @param consumer the bi-consumer called with the result (potentially null) or the failure.
     * @return the new {@link Uni}
     */
    Uni<T> doFinally(BiConsumer<T, Throwable> consumer);


    // Error Management

    /**
     * Produces a new {@link Uni} invoking the given function when this {@link Uni} propagates a failure. The function
     * can transform the received failure into another exception.
     *
     * @param mapper the mapper function, must not be {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> onErrorMap(Function<? super Throwable, ? extends Throwable> mapper);

    /**
     * Produces a new {@link Uni} invoking the given function when this {@link Uni} propagates a failure. The function
     * produces a result (potentially {@code null}) used as result by the produced {@link Uni}. Note that if the
     * mapper throws an exception, the produced {@link Uni} propagates the failure.
     *
     * @param mapper the mapper function, must not be {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> onErrorResume(Function<? super Throwable, ? extends T> mapper);

    /**
     * Produces a new {@link Uni} invoking the given function when this {@link Uni} propagates a failure. The function
     * produces a fallback {@link Uni}. Note that if the mapper throws an exception, the produced {@link Uni} propagates
     * the failure.
     *
     * @param mapper the mapper function, must not be {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> onErrorSwitch(Function<? super Throwable, Uni<? extends T>> mapper);

    /**
     * Produces a new {@link Uni} producing the given value (potentially {@code null}) when this {@link Uni} propagates
     * a failure.
     *
     * @param defaultValue the value replacing the failure, can be {@code null}
     * @return the new {@link Uni}
     */
    Uni<T> onErrorReturn(T defaultValue);

    /**
     * Produces a new {@link Uni} producing a value (potentially {@code null}) using the given supplier when
     * this {@link Uni} propagates a failure.
     *
     * @param supplier the supplier producing the value, must not be {@code null} but the returned value can be
     *                 {@code null}. If the supplier throws an exception, this exception is propagated into the
     *                 returned {@code Uni}.
     * @return the new {@link Uni}
     */
    Uni<T> onErrorReturn(Supplier<? extends T> supplier);

    /**
     * Retries the resolution of this {@link Uni} indefinitely. The process of retrying uses a re-subscription.
     *
     * @return a new {@link Uni} retrying indefinitely to subscribe to this {@link Uni} until it gets a success.
     */
    Uni<T> retry();

    /**
     * Retries the resolution of this {@link Uni} at most {@code count} times. The process of retrying uses a
     * re-subscription.
     *
     * @param count the number of attempts, must be greater than 0.
     * @return a new {@link Uni} retrying at most {@code count} time to subscribe to this {@link Uni} until it gets a
     * success. When the count is reached, the last failure is propagated.
     */
    Uni<T> retry(int count);


    // Exports

    /**
     * Creates a {@link Publisher} for the current {@link Uni}. The created {@link Publisher} emits:
     * <ul>
     * <li>a single item {@code T} followed by the end of stream signal if the item is not {@code null}</li>
     * <li>the end of stream signal if the item resolved by this {@code Uni} is {@code null}</li>
     * <li>the failure signal if the resolution of this {@link Uni} propagate a failure</li>
     * </ul>
     * <p>
     * Note that subscribing to the returned {@link Publisher} does not trigger the computation of this {@link Uni}. It
     * must {@link org.reactivestreams.Subscription#request(long)} an item to trigger the resolution.
     *
     * @return a {@link Publisher} containing at most one item.
     */
    Publisher<T> toPublisher();

}
