package io.smallrye.reactive.streams.api.groups;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniEmitter;
import io.smallrye.reactive.streams.api.UniSubscriber;
import org.reactivestreams.Publisher;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Group to create {@link Uni} instances from various sources.
 */
public interface UniFromGroup {

    /**
     * Creates a {@link Uni} from the given {@link CompletionStage} or {@link CompletableFuture}.
     * The produced {@code Uni} emits the result of the passed  {@link CompletionStage}. If the {@link CompletionStage}
     * never completes (or failed), the produced {@link Uni} would not emit a value or a failure.
     * <p>
     * Cancelling the subscription on the produced {@link Uni} cancels the passed {@link CompletionStage}
     * (calling {@link CompletableFuture#cancel(boolean)} on the future retrieved using
     * {@link CompletionStage#toCompletableFuture()}.
     * <p>
     * If the stage has already been completed (or failed), the produced {@link Uni} sends the result or failure
     * immediately after subscription. If it's not the case, the subscriber's callbacks are called on the thread used
     * by the passed {@link CompletionStage}.
     *
     * @param stage the stage, must not be {@code null}
     * @param <T>   the type of result
     * @return the produced {@link Uni}
     */
    <T> Uni<T> completionStage(CompletionStage<? extends T> stage);

    /**
     * Creates a {@link Uni} from the given {@link CompletionStage} or {@link CompletableFuture}. The future is
     * created by invoking the passed {@link Supplier} at subscription time.
     * <p>
     * The produced {@code Uni} emits the result of the passed  {@link CompletionStage}. If the {@link CompletionStage}
     * never completes (or failed), the produced {@link Uni} would not emit a value or a failure.
     * <p>
     * Cancelling the subscription on the produced {@link Uni} cancels the passed {@link CompletionStage}
     * (calling {@link CompletableFuture#cancel(boolean)} on the future retrieved using
     * {@link CompletionStage#toCompletableFuture()}.
     * <p>
     * If the produced stage has already been completed (or failed), the produced {@link Uni} sends the result or failure
     * immediately after subscription. If it's not the case the subscriber's callbacks are called on the thread used
     * by the passed {@link CompletionStage}.
     * <p>
     * If the passed supplier returns {@code null}, the returned {@link Uni} propagates a {@link NullPointerException}.
     * If the passed supplier throws an exception, the returned {@link Uni} propagates the exception as failure.
     *
     * @param supplier the supplier, must not be {@code null}, must not produce {@code null}
     * @param <T>      the type of result
     * @return the produced {@link Uni}
     */
    <T> Uni<T> completionStage(Supplier<? extends CompletionStage<? extends T>> supplier);

    /**
     * Creates a {@link Uni} from the passed {@link Publisher}.
     * <p>
     * The produced {@link Uni} emits the first value emitted by the passed {@link Publisher}.
     * If the publisher emits multiple values, others are dropped. If the publisher emits a failure after a value, the
     * failure is dropped. If the publisher emits the end of stream signal before a value, the produced {@link Uni} is
     * resolved with {@code null}.
     * <p>
     * When a subscriber subscribes to the produced {@link Uni}, it subscribes to the {@link Publisher} and requests
     * {@code 1} item. When the first signal is received, the subscription is cancelled. Note that each Uni's subscriber
     * would produce a new subscription.
     * <p>
     * If the Uni's observer cancels its subscription, the subscription to the {@link Publisher} is also cancelled.
     *
     * @param publisher the publisher, must not be {@code null}
     * @param <T>       the type of item
     * @return the produced {@link Uni}
     */
    <T> Uni<T> publisher(Publisher<? extends T> publisher);

    /**
     * Creates a new {@link Uni} that completes immediately after being subscribed to with the specified (potentially
     * {@code null}) value. The value is retrieved, at subscription time, using the passed {@link Supplier}. Unlike
     * {@link #deferred(Supplier)}, the supplier produces a value (result) and not an {@link Uni}.
     * <p>
     * If the supplier produces {@code null}, {@code null} is used as value.
     * If the supplier throws an exception, this exception is propagated as failure.
     *
     * @param supplier the supplier, must not be {@code null}, can produce {@code null}
     * @return the new {@link Uni}
     */
    <T> Uni<T> value(Supplier<? extends T> supplier);

    /**
     * Creates a new {@link Uni} that completes immediately after being subscribed to with the specified (potentially
     * {@code null}) value.
     *
     * @param value the value, can be {@code null}
     * @return the new {@link Uni}
     */
    <T> Uni<T> value(T value);

    /**
     * Creates a new {@link Uni} that completes immediately after being subscribed to with the specified value if
     * {@link Optional#isPresent()} or {@code null} otherwise.
     *
     * @param optional the optional, must not be {@code null}
     * @param <T>      the type of the produced item
     * @return the new {@link Uni}
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    <T> Uni<T> optional(Optional<T> optional);

    /**
     * Creates a new {@link Uni} that completes immediately after being subscribed to with the specified value if
     * {@link Optional#isPresent()} or {@code null} otherwise. Unlike {@link #optional(Optional)}, the passed
     * {@link Supplier} is called at subscription time and the result is used to complete the {@link Uni} immediately.
     * If the supplier throws an exception or {@code null}, the produced {@link Uni} propagates a failure.
     *
     * @param optional the supplier, must not be {@code null}, must not return {@code null}
     * @param <T>      the type of the produced item
     * @return the new {@link Uni}
     */
    <T> Uni<T> optional(Supplier<Optional<T>> optional);

    //TODO Javadoc
    Uni<Void> delay(Duration duration, ScheduledExecutorService executor);

    /**
     * Creates a {@link Uni} deferring the logic to the given consumer. The consumer can be used with callback-based
     * APIs to signal at most one value (potentially {@code null}), or a failure signal.
     * <p>
     * Using this method, you can produce a {@link Uni} listener or callbacks APIs. You register the listener in
     * the consumer and emits the value / failure on events. Don't forget to unregister the listener on cancellation.
     * Note that the emitter only forwards the first signal, subsequent signals are dropped.
     * <p>
     * If the consumer throws an exception, this exception is propagated (dropped if the first signal was already sent).
     *
     * @param consumer callback receiving the {@link UniEmitter} and producing signals downstream. The callback is
     *                 called for each subscriber (at subscription time). Must not be {@code null}
     * @param <T>      the type of item
     * @return the produced {@link Uni}
     */
    <T> Uni<T> emitter(Consumer<UniEmitter<? super T>> consumer);

    /**
     * Creates a {@link Uni} that will {@link Supplier#get supply} a target {@link Uni} to subscribe to for
     * each {@link UniSubscriber} downstream. The supplier is called for each subscriber at subscription time.
     * <p>
     * In practice, it defers the {@link Uni} creation at subscription time and allows each subscriber to get different
     * {@link Uni}. So, it does not create the {@link Uni} until an {@link UniSubscriber subscriber} subscribes, and
     * creates a fresh {@link Uni} for each subscriber.
     * <p>
     * Unlike {@link #value(Supplier)}, the supplier produces an {@link Uni} (and not a value).
     * <p>
     * If the supplier throws an exception, the exception is propagated. If the supplier produces {@code null}, a
     * {@link NullPointerException} is propagated.
     *
     * @param supplier the supplier, must not be {@code null}, must not produce {@code null}
     * @param <T>      the type of item
     * @return the produced {@link Uni}
     */
    <T> Uni<T> deferred(Supplier<? extends Uni<? extends T>> supplier);

    /**
     * Creates a {@link Uni} that emits the passed failure immediately after being subscribed to.
     *
     * @param failure the failure, must not be {@code null}
     * @param <T>     the type of the {@link Uni}, must be explicitly set as in {@code Uni.<String>failed(exception);}
     * @return the new {@link Uni}
     */
    <T> Uni<T> failure(Throwable failure);

    /**
     * Creates a {@link Uni} that emits a failure produced using the passed supplier immediately after being subscribed
     * to. The supplier is called at subscription time, and produce an instance of {@link Throwable}. If the supplier
     * throws an exception, this exception is propagated. If the supplier produces {@code null}, a
     * {@link NullPointerException} is propagated.
     *
     * @param supplier the supplier producing the failure, must not be {@code null}, must not produce {@link null}
     * @param <T>      the type of the {@link Uni}, must be explicitly set as in
     *                 {@code Uni.<String>failed(() -> exception);}
     * @return the new {@link Uni}
     */
    <T> Uni<T> failure(Supplier<Throwable> supplier);


    /**
     * Creates a {@link Uni} that will never signal any result, or failure, essentially running indefinitely.
     *
     * @param <T> the type of item
     * @return a never completing {@link Uni}
     */
    <T> Uni<T> nothing();

    /**
     * Equivalent to {@link #value(Object)} called with {@code null}.
     *
     * @return a {@link Uni} immediately calling {@link UniSubscriber#onResult(Object)} with {@code null} just
     * after subscription.
     */
    Uni<Void> nullValue();

}
