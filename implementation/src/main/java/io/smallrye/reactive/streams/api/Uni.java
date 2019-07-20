package io.smallrye.reactive.streams.api;

import io.smallrye.reactive.streams.api.groups.*;
import io.smallrye.reactive.streams.api.impl.UniFromGroupImpl;
import io.smallrye.reactive.streams.api.tuples.Pair;
import org.reactivestreams.Publisher;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A {@link Uni} represent a lazy asynchronous action. It follows a subscription pattern, meaning the the action
 * is only triggered on subscription.
 * <p>
 * A {@link Uni} can have two outcomes:
 * <ol>
 * <li>A result, potentially {@code null}</li>
 * <li>A failure</li>
 * </ol>
 * <p>
 * To trigger the computation, a {@link UniSubscriber} must subscribe to the Uni. It will receive the result or failure
 * once they are emitted by the observed Uni. A subscriber receives (asynchronously) a {@link UniSubscription} and
 * can cancel the demand at any time. Note that cancelling after having received the outcome is a no-op.
 * <p>
 * The {@link Uni} class proposes a set of <em>groups</em> for various tasks:
 *
 * <ul>
 * <li>{@link UniFromGroup Uni.from()} let's you create instance of <code>Uni</code> from different sources</li>
 * <li>{@link UniMapGroup uni.map()} let's you transform the result of the failure in a synchronous manner</li>
 * <li>{@link Uni#flatMap(Function) uni.flatMap()}</code> let's you transform the result into another Uni, it allows
 * asynchronous composition</li>
 * <li>{@link UniPeekGroup uni.on()} let's you <em>peek</em> to the different signals (result, failure) and events
 * (subscription and cancellation)</li>
 * <li>{@link UniSubscribeGroup uni.subscribe()} let's you register a subscriber and trigger the computation</li>
 * <li>{@link UniAwaitGroup uni.await()} let's you block and wait for the outcome</li>
 * <li>{@link UniNullGroup uni.onNull()} let's you handle {@code null} values</li>
 * <li>{@link UniRecoveryGroup uni.recover()} let's you handle failures and recovery on failure (such as retry)</li>
 * <li>{@link UniOnTimeoutGroup uni.onTimeout()} let's you handle timeout and recovery on timeout</li>
 * <li>{@link UniIgnoreGroup uni.ignore()} let's you ignore the result, but continue the processing with
 * <em>something</em> else</li>
 * <li>{@link UniAwaitGroup uni.and()} let's you compose several unis together and join their results</li>
 * </ul>
 *
 * @param <T> the type of item produced by the {@link Uni}
 */
public interface Uni<T> {

    /**
     * Creates a new {@link Uni} from various sources such as {@link CompletionStage}, {@link UniEmitter}, direct values,
     * {@link Exception}...
     *
     * <p>Examples:</p>
     * <pre><code>
     * Uni.from().value(1); // Emit 1 at subscription time
     * Uni.from().value(() -> x); // Emit x at subscription time, the supplier is invoked for each subscription
     * Uni.from().completionState(cs); // Emit the result from this completion stage
     * Uni.from().completionState(() -> cs); // Emit the result from this completion stage, the stage is not created before subscription
     * Uni.from().failure(exception); // Emit the failure at subscription time
     * Uni.from().deferred(() -> Uni.from().value(x)); // Defer the uni creation until subscription. Each subscription can produce a different uni
     * Uni.from().nullValue(); // Emit null at subscription time
     * Uni.from().nothing(); // Create a Uni not emitting any signal
     * Uni.from().publisher(publisher); // Create a Uni from a Reactive Streams Publisher
     * </code></pre>
     *
     * @return the factory used to create {@link Uni} instances.
     * @see UniFromGroup
     */
    static UniFromGroup from() {
        return UniFromGroupImpl.INSTANCE;
    }

    /**
     * Creates a new {@link Uni} that completes immediately after being subscribed to with the specified (potentially
     * {@code null}) value. This method is equivalent to <code>Uni.from().value(value)</code>.
     *
     * @param value the value, can be {@code null}
     * @param <T>   the type of the produced item
     * @return the new {@link Uni}
     * @see #from()
     */
    static <T> Uni<T> of(T value) {
        return from().value(value);
    }

    /**
     * Creates a {@link Uni} forwarding the first signal (value, {@code null} or failure). It behaves like the fastest
     * of these competing unis. If the set of competing unis is empty, the resulting {@link Uni} gets a {@code null}
     * result just after subscription.
     * <p>
     * This method subscribes to the set of {@link Uni}. When one of the {@link Uni} resolves successfully or with
     * a failure, the signals is propagated to the returned {@link Uni}. Also the other subscriptions are cancelled.
     * Note that the callback from the subscriber are called on the thread used to resolve the winning {@link Uni}.
     * Use {@link #publishOn(Executor)} to change the thread.
     * <p>
     * If the subscription to the returned {@link Uni} is cancelled, the subscription to the {@link Uni unis} from the
     * {@code iterable} are also cancelled.
     *
     * @return the object to enlist the candidates
     * @see Uni#or <code>Uni.or()</code> for the equivalent operator on Uni instances
     */
    static UniAnyGroup any() {
        return UniAnyGroup.INSTANCE;
    }

    /**
     * Combines several {@link Uni} into a new {@link Uni} that will be fulfilled when all {@link Uni} are
     * resolved successfully aggregating their values into a {@link io.smallrye.reactive.streams.api.tuples.Tuple},
     * or using a combinator function.
     * <p>
     * The produced {@link Uni} forwards the failure if one of the {@link Uni Unis} produces a failure. This will
     * cause the other {@link Uni} to be cancelled, expect if {@code awaitCompletion()} is invoked, which delay the failure
     * propagation when all {@link Uni}s have completed or failed.
     *
     * @see Uni#and <code>Uni.and()</code> for the equivalent operator on Uni instances
     */
    static UniZipGroup zip() {
        return UniZipGroup.INSTANCE;
    }

    /**
     * Requests the {@link Uni} to start resolving the result and allows configuring how the signals are propagated
     * (using a {@link UniSubscriber}, callbacks, or a {@link CompletionStage}. Unlike {@link #await()}, this method
     * configures non-blocking retrieval of the result and failure.
     *
     * <p>Examples:</p>
     * <pre><code>
     *     Uni<String> uni = ...;
     *
     *    Subscription sub = uni.subscribe().with( // The return subscription can be used to cancel the operation
     *              result -> {},           // Callback calls on result
     *              failure -> {}           // Callback calls on failure
     *    );
     *
     *    UniSubscriber<String> myUniSubscriber = ...
     *    uni.subscribe().withSubscriber(myUniSubscriber); // Subscribes to the Uni with the passed subscriber
     *
     *    CompletableFuture future = uni.subscribe().asCompletableFuture(); // Get a CompletionStage receiving the result or failure
     *    // Cancelling the returned future cancels the subscription.
     * </code></pre>
     *
     * @return the object to configure the subscription.
     * @see #await() <code>uni.await() </code>for waiting (blocking the caller thread) until the resolution of the observed Uni.
     */
    UniSubscribeGroup<T> subscribe();

    /**
     * Awaits (blocking the caller thread) until the result or a failure is emitted by the observed {@link Uni}.
     * If the observed uni fails, the failure is thrown. In the case of a checked exception, the exception is wrapped
     * into a {@link java.util.concurrent.CompletionException}.
     *
     * <p>Examples:</p>
     * <pre><code>
     * Uni&lt;T&gt; uni = ...;
     * T res = uni.await().indefinitely(); // Await indefinitely until it get the result.
     * T res = uni.await().atMost(Duration.ofMillis(1000)); // Awaits at most 1s. After that, a TimeoutException is thrown
     * Optional<T> res = uni.await().asOptional().indefinitely(); // Retrieves the result as an Optional, empty if the result is null
     * </code></pre>
     *
     * @return the object to configure the retrieval.
     */
    UniAwaitGroup<T> await();

    /**
     * Adds behavior when various signals (result and failure) are emitted and events (subscription and cancellation) are
     * received.
     *
     * <p>Examples:</p>
     * <pre><code>
     *     Uni&lt;T&gt; upstream = ...;
     *     Uni&lt;T&gt; uni = upstream
     *      .on().result(result-> {}) // called when the observed uni emits a result
     *      .on().failure(failure -> {}) // called when the observed uni emits a failure
     *      .on().terminate((result, failure) -> {}) // called when the observed uni emits a result or failure
     *      .on().subscription(sub -> {}) // called when a subscriber subscribes to the Uni
     *      .on().cancellation(() -> {}) // called when a subscriber cancels a subscription
     * </code></pre>
     *
     * @return the object to configure the actions.
     */
    UniPeekGroup<T> on();

    /**
     * Add specific behavior when the observed {@link Uni} emits a {@code null} result. While {@code null} is a valid
     * value, it may require specific processing. This group of operators allows implementing this specific behavior.
     *
     * <p>Examples:</p>
     * <pre><code>
     *     Uni&lt;T&gt; upstream = ...;
     *     Uni&lt;T&gt; uni = ...;
     *     uni = upstream.on().onNull().continueWith(anotherValue) // use the fallback value if upstream emits null
     *     uni = upstream.on().onNull().fail() // propagate a NullPointerException if upstream emits null
     *     uni = upstream.on().onNull().failWith(exception) // propagate the given exception if upstream emits null
     *     uni = upstream.on().onNull().switchTo(another) // switch to another uni if upstream emits null
     * </code></pre>
     *
     * @return the object to configure the behavior when receiving {@code null}
     */
    UniNullGroup<T> onNull();

    /**
     * Transforms the results and failures emitted by this {@link Uni} by applying synchronous function to them.
     *
     * <p>Examples:</p>
     * <pre><code>
     *     Uni&lt;T&gt; upstream = ...;
     *     Uni&lt;T&gt; uni = ...;
     *     uni = upstream.map().result(t -> ...); // transforms the result using the given function
     *     uni = upstream.map().to(MyOtherClass.class); // cast the result to the given class
     *     uni = upstream.map().toBoolean(predicate); // test the result using the given predicate and emits the result as a boolean
     *     uni = upstream.map().failure(failure -> ...); // transforms the failure using the given function
     *     uni = upstream.map().toFailure(t -> ...) // transforms the result into a failure
     * </code></pre>
     * <p>
     * Transforming failure is not a recovery action. See {@link #recover()} to handle failure gracefully.
     *
     * @return the object to configure the functions to apply.
     * @see #map(Function) <code>uni.map(function)</code>as a shorter version for {@code map().result(...)}
     * @see #flatMap(Function) <code>uni.flatMap(function)</code> for asynchronous operations
     */
    UniMapGroup<T> map();

    /**
     * Transforms the result (potentially null) emitted by this {@link Uni} by applying a (synchronous) function to it.
     * This method is equivalent to {@code uni.map().onResult(x -> ...)}
     * For asynchronous composition, look at flatMap.
     *
     * @param mapper the mapper function, must not be {@code null}
     * @param <O>    the output type
     * @return a new {@link Uni} computing a result of type {@code <O>}.
     */
    <O> Uni<O> map(Function<T, O> mapper);

    /**
     * Produces a new {@link Uni} invoking the {@link UniSubscriber#onResult(Object)}  and
     * {@link UniSubscriber#onFailure(Throwable)} on the supplied {@link Executor}.
     * <p>
     * This operator influences the threading context where the rest of the operators in the downstream chain it will
     * execute, up to a new occurrence of {@code publishOn(Executor)}.
     *
     * @param executor the executor to use, must not be {@code null}
     * @return a new {@link Uni}
     */
    Uni<T> publishOn(Executor executor);

    /**
     * Run {@code subscribe} (on the upstream), {@code onSubscribe} (on the subscriber) on the specified {@link Executor}.
     *
     * @param executor the executor to use, must not be {@code null}
     * @return a new {@link Uni}
     */
    Uni<T> subscribeOn(Executor executor);

    /**
     * Caches the completion (result or failure) of this {@link Uni} and replays it for all further {@link UniSubscriber}.
     *
     * @return the new {@link Uni}. Unlike regular {@link Uni}, re-subscribing to this {@link Uni} does not re-compute
     * the outcome but replayed the cached signals.
     */
    Uni<T> cache();

    /**
     * Combines a set of {@link Uni} into a joined result. This result can be a {@code Tuple} or the result of a
     * combinator function.
     * <p>
     * If one of the combine {@link Uni} propagates a failure, the other sources are cancelled, and the resulting
     * {@link Uni} propagates the failure. If {@link AndGroup2#awaitCompletion()}  is called, it waits for the
     * completion of all the {@link Uni unis} before propagating the failure. If more than one {@link Uni} failed,
     * a {@link CompositeException} is propagated, wrapping the different failures.
     * <p>
     * Depending on the number of participant, the produced {@link io.smallrye.reactive.streams.api.tuples.Tuple} is
     * different from {@link Pair} to {@link io.smallrye.reactive.streams.api.tuples.Tuple5}. For more participants,
     * use {@link AndGroup#unis(Uni[])} or {@link AndGroup#unis(Iterable)}.
     *
     * @return the object to configure the join
     * @see Uni#zip() <code>Uni.zip()</code> for the equivalent static operator
     */
    AndGroup<T> and();

    /**
     * Combines the result of this {@link Uni} with the result of {@code other} into a {@link Pair}.
     * If {@code this} or {@code other} fails, the other resolution is cancelled.
     *
     * @param other the other {@link Uni}, must not be {@code null}
     * @return the combination of the 2 results.
     * @see #and() <code>and</code> for more options on the combination of results
     * @see Uni#zip() <code>Uni.zip()</code> for the equivalent static operator
     */
    <T2> Uni<Pair<T, T2>> and(Uni<T2> other);

    /**
     * Delays the completion (result or failure) of the {@link Uni} by a given duration.
     *
     * @return the object to configure the delay and executor.
     */
    UniDelayGroup<T> delay();

    /**
     * Transform the result resolved by this {@link Uni} asynchronously, returning the signals emitted by another
     * {@link Uni} produced by the given {@code mapper}.
     * <p>
     * The mapper is called with the result of this {@link Uni} and returns an {@link Uni}, possibly using another
     * result type. The signals of the produced {@link Uni} are forwarded to the {@link Uni} returned by this method.
     *
     * @param mapper the function called with the result of the this {@link Uni} and producing the {@link Uni}
     * @param <O>    the type of result
     * @return a new {@link Uni} with an asynchronously mapped result or failure
     */
    <O> Uni<O> flatMap(Function<? super T, ? extends Uni<? extends O>> mapper);

    /**
     * Transforms the result resolved by this {@link Uni} asynchronously, returning a {@link Uni} emitting the signals
     * instructed by the passed {@link UniEmitter}.
     *
     * @param consumer the consumer called with the result of the this {@link Uni} and with an {@link UniEmitter} used
     *                 to emit the signals from the resulting {@link Uni}. Must not be {@code null}.
     * @param <O>      the type of result
     * @return this {@link Uni}
     */
    <O> Uni<O> flatMap(BiConsumer<? super T, UniEmitter<? super O>> consumer);

    /**
     * Creates a {@link Uni} ignoring the result of the current {@link Uni} and continuing with either
     * {@link UniIgnoreGroup#andContinueWith(Object) another result}, {@link UniIgnoreGroup#andFail() a failure},
     * or {@link UniIgnoreGroup#andSwitchTo(Uni) another Uni}. The produced {@link Uni} propagates the failure
     * signal as this {@link Uni}.
     *
     * <p>Examples:</p>
     * <pre><code>
     *     Uni&lt;T&gt; upstream = ...;
     *     uni = upstream.ignore().andSwitchTo(other) // Ignore the result of upstream and switch to another uni
     *     uni = upstream.ignore().andContinueWith(value) // Ignore the result of upstream and emit `value` as result
     * </code></pre>
     *
     * @return the {@link UniIgnoreGroup} to configure the action.
     */
    UniIgnoreGroup<T> ignore();

    /**
     * Composes this {@link Uni} with a set of {@link Uni} passed to {@link UniOrGroup#unis(Uni[])} to produce a new
     * {@link Uni} forwarding the first signal (value, {@code null} or failure). It behaves like the fastest
     * of these competing unis.
     * <p>
     * The process subscribes to the set of {@link Uni}. When one of the {@link Uni} resolves successfully or with
     * a failure, the signals is propagated to the returned {@link Uni}. Also the other subscriptions are cancelled.
     * Note that the callback from the subscriber are called on the thread used to resolve the winning {@link Uni}.
     * Use {@link #publishOn(Executor)} to change the thread.
     * <p>
     * If the subscription to the returned {@link Uni} is cancelled, the subscription to the {@link Uni unis} from the
     * {@code iterable} are also cancelled.
     *
     * @return the object to enlist the participants
     * @see #any() <code>Uni.any</code> for a static version of this operator, like <code>Uni first = Uni.any().of(uni1, uni2);</code>
     */
    UniOrGroup or();

    /**
     * Produces a {@link Uni} reacting when a time out is reached.
     * This {@link Uni} detects if this  {@link Uni }does not resolve successfully (with a value or {@code null})
     * before the configured timeout.
     * <p>
     * Examples:
     * <code>
     * uni.onTimeout().of(Duration.ofMillis(1000).fail() // Propagate a TimeOutException
     * uni.onTimeout().of(Duration.ofMillis(1000).recover().withValue("fallback") // Inject a fallback result on timeout
     * uni.onTimeout().of(Duration.ofMillis(1000).on(myExecutor)... // Configure the executor calling on timeout actions
     * uni.onTimeout().of(Duration.ofMillis(1000).recover().withRetry().atMost(5) // Retry five times
     * </code>
     *
     * @return the on timeout group
     */
    UniOnTimeoutGroup<T> onTimeout();
    //TODO CES - thinking about renaming it to uni.onNoResult().after(duration).[continueWith(...), fail...]


    /**
     * Produces a {@link Uni} handling failures propagated by the upstream {@link Uni}.
     * This {@link Uni} receives the failure and allow handling it gracefully.
     * <p>
     * Examples:
     * <code>
     * uni.recover().withResult(x) // use a fallback result on failure
     * uni.recover().fromFailure(IOException.class).withResult(x) // use a fallback in case of an IOException
     * uni.recover().withUni(other) // switch to the given uni on failure
     * uni.recover().withRetry().atMost(5) // retry at most 5 times
     * </code>
     *
     * @return the object to configure the recovery policy
     */
    UniRecoveryGroup<T> recover();

    // TODO Create a group for the to()

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

    /**
     * Transforms this {@link Uni} into an instance of the given class. The transformations acts as follows:
     * <ol>
     * <li>If this is an instance of O - return this</li>
     * <li>If there is on the classpath, an implementation of {@link io.smallrye.reactive.streams.api.adapter.UniAdapter}
     * for the type O, the adapter is used (invoking {@link io.smallrye.reactive.streams.api.adapter.UniAdapter#adaptTo(Uni)})</li>
     * <li>If O has a {@code fromPublisher} method, this method is called with a {@link Publisher} produced
     * using {@link #toPublisher()}</li>
     * <li>If O has a {@code instance} method, this method is called with a {@link Publisher} produced
     * using {@link #toPublisher()}</li>
     * </ol>
     *
     * @param clazz the output class
     * @param <O>   the produced type
     * @return an instance of O
     * @throws RuntimeException if the transformation fails.
     */
    <O> O to(Class<O> clazz);

    /**
     * Transforms this {@link Uni} into an instance of {@code O} using the given {@code transformer} function.
     *
     * @param transformer
     * @param <O>
     */
    <O> O to(Function<? super Uni<T>, O> transformer);

    /**
     * Transforms this {@link Uni} into an instance of {@link Multi}.
     * <p>
     * If this {@link Uni} resolves with a non-null value, this value is emitted in the {@link Multi}, followed by
     * completion.
     * If this {@link Uni} resolves with a @{code null} value, the returned {@link Multi} would be completed empty.
     * If this {@link Uni} receives a failure, the failure is propagated to the {@link Multi}.
     *
     * @param <O> the type of item
     * @return the produced {@link Multi}, never {@code null}
     * @see #toPublisher()
     * @see UniFromGroup#publisher(Publisher)
     */
    <O> Multi<O> toMulti();


}
