package io.smallrye.reactive.streams.api;

import io.smallrye.reactive.streams.api.impl.*;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A {@link Uni} represent a lazy asynchronous action. Once triggered, by a {@link UniSubscriber}, it starts computing
 * and emits the result or failures.
 * <p>
 * The {@link Uni} type proposes a set of operators to chain operations.
 *
 * @param <T>
 */
public interface Uni<T> {


    // TODO Javadoc
    static <T> Uni<T> of(T value) {
        return new OfUniOperator<>(value);
    }

    static <T> Uni<T> fromOptional(Optional<T> value) {
        return new OfUniOperator<>(value);
    }

    // TODO Javadoc
    static <T> Uni<T> failed(Throwable e) {
        return new FailedUniOperator<>(Objects.requireNonNull(e, "The passed exception must not be `null`"));
    }

    // TODO Javadoc
    static <T> Uni<T> fromCompletionStage(CompletionStage<T> stage) {
        return new FromCompletionStageUniOperator<>(Objects.requireNonNull(stage, "The passed completion stage must not be `null`"));
    }

    // TODO Javadoc
    static <T> Uni<T> fromPublisher(Publisher<T> publisher) {
        return fromPublisher(ReactiveStreams.fromPublisher(publisher));
    }

    static <T> Uni<T> fromPublisher(PublisherBuilder<T> publisher) {
        return new FromPublisherUniOperator<>(Objects.requireNonNull(publisher, "The passed publisher stage must not be `null`"));
    }

    static <T> Uni<T> defer(Supplier<? extends Uni<? extends T>> supplier) {
        return new DeferredUniOperator<>(Objects.requireNonNull(supplier, "The passed supplier must not be `null`"));
    }


    /**
     * Requests the {@link Uni} to start computing the result.
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
