package io.smallrye.reactive.converters;

import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Converts a specific reactive types from and to {@link CompletionStage} and {@code Publisher}.
 *
 * @param <T> the converted type.
 *            <p>
 *            Implementations must be tested against the TCK by extending the test case from the
 *            {@code io.smallrye.reactive.converters.tck} packages.
 */
public interface ReactiveTypeConverter<T> {

    /**
     * Transforms an instance of {@code T} to a {@link CompletionStage} completed with an {@link Optional}.
     * Each converter instances can use specific rules, however the following set of rules are mandatory:
     *
     * <ul>
     * <li>The returned {@link CompletionStage} must never be {@code null}.</li>
     * <li>The returned {@link CompletionStage} completes with the first emitted value wrapped into an
     * {@link Optional} instance, as a consequence it must never be completed with {@code null}.</li>
     * <li>If the passed {@code instance} emits several values, only the first one is considered, others are
     * discarded.</li>
     * <li>If the passed {@code instance} fails before emitting a value, the returned {@link CompletionStage}
     * completes with this failure.</li>
     * <li>If the passed {@code instance} does not emit any value and does not fail or complete, the returned
     * {@code CompletionStage} does not complete.</li>
     * <li>If the passed {@code instance} completes <strong>before</strong> emitting a value, the
     * {@link CompletionStage} is completed with an empty {@link Optional}.</li>
     * <li>If the passed {@code instance} emits {@code null} as first value (if supported), the
     * {@link CompletionStage} is completed with an empty {@link Optional}. As a consequence, there are no
     * differences between an {@code instance} emitting {@code null} as first value or completing without emitting
     * a value. If the {@code instance} does not support emitting {@code null} values, the returned
     * {@link CompletionStage} must be completed with a failure.</li>
     * </ul>
     *
     * @param instance the instance to convert to a {@link CompletionStage}. Must not be {@code null}.
     * @param <X>      the type wrapped into the resulting {@link Optional} emitted by the returned {@link CompletionStage}.
     *                 It is generally the type of data emitted by the passed {@code instance}.
     * @return a {@code non-null} {@link CompletionStage}.
     */
    <X> CompletionStage<Optional<X>> toCompletionStage(T instance);

    /**
     * Transforms an instance of {@code T} to a {@link Publisher}.
     * Each converter instances can use specific rules, however the following set of rules are mandatory:
     *
     * <ul>
     * <li>The returned {@link Publisher} must never be {@code null}.</li>
     * <li>All values emitted by the {@code instance} are emitted by the returned {@link Publisher}.</li>
     * <li>If the {@code instance} emits a failure, {@link Publisher} propagates the same failure and
     * terminates.</li>
     * <li>If the {@code instance} completes, {@link Publisher} also completes.</li>*
     * <li>If the passed {@code instance} does not emit any value and does not fail or complete, the returned
     * {@code Publisher} does not send any signals or values.</li>
     * <li>If the passed {@code instance} completes <strong>before</strong> emitting a value, the {@link Publisher}
     * also completes empty.</li>
     * <li>If the passed {@code instance} emits {@code null}, the {@link Publisher} must send a failure
     * ({@link NullPointerException}.</li>
     * <li>If the {@code instance} support back-pressure, the resulting {@link Publisher} must enforce
     * back-pressure. When the {@code instance} does not support back-pressure, the {@link Publisher} consumes
     * the data without back-pressure using an unbounded-buffer. In other words, this operation is a pass-through
     * for back-pressure and its behavior is determined by the back-pressure behavior of the passed
     * {@code instance}.</li>
     * </ul>
     *
     * @param instance the instance to convert to a {@link Publisher}. Must not be {@code null}.
     * @param <X>      the type emitted by the returned {@link Publisher}. It
     *                 is generally the type of data emitted by the passed {@code instance}.
     * @return a {@code non-null} {@link Publisher}.
     */
    <X> Publisher<X> toRSPublisher(T instance);

    /**
     * Transforms an instance of {@link CompletionStage} to an instance of {@code T}. The value emitted by {@code T}
     * depends on the completion of the passed {@link CompletionStage}. Each converter instances can use specific rules,
     * however the following set of rules are mandatory:
     *
     * <ul>
     * <li>The returned {@code T} must never be {@code null}.</li>
     * <li>If the passed {@link CompletionStage} never completes, no values are emitted by the returned
     * {@code T}.</li>
     * <li>If the passed {@link CompletionStage} redeems a {@code null} value, and if {@code T} support {@code null}
     * values, {@code null} is emitted by the returned instance of {@code T}.</li>
     * <li>If the passed {@link CompletionStage} redeems a {@code null} value, and if {@code T} does not support
     * {@code null} values, a failure is emitted by the returned instance of {@code T}.</li>
     * <li>If the passed {@link CompletionStage} redeems a {@code non-null} value, the value is emitted by the
     * returned instance of {@code T}.</li>
     * <li>If the passed {@link CompletionStage} is completed with a failure, the same failure is emitted by
     * the returned instance of {@code T}.</li>
     * <li>If the passed {@link CompletionStage} is cancelled before having completed, the
     * {@link java.util.concurrent.CancellationException} must be emitted by the returned instance.</li>
     * </ul>
     * <p>
     * Implementations must not expect the {@link CompletionStage} to be instances of
     * {@link java.util.concurrent.CompletableFuture}.
     * <p>
     * Implementations may decide to adapt the emitted result when receiving container object such as {@link Optional}.
     *
     * @param cs  the instance of {@link CompletionStage}, must not be {@code null}
     * @param <X> the type of result provided by the {@link CompletionStage}
     * @return the instance of T, generally emitting instances of {@code X}.
     */
    <X> T fromCompletionStage(CompletionStage<X> cs);

    /**
     * Transforms an instance of {@code T} to a {@link Publisher}.
     * Each converter instances can use specific rules, however the following set of rules are mandatory:
     *
     * <ul>
     * <li>The returned {@link Publisher} must never be {@code null}.</li>
     * <li>If the instance of {@code T} emits a single value, the returned {@link Publisher} emits the same value
     * and completes.</li>
     * <li>If the instance of {@code T} does not emits value, sends the completion signal, the returned
     * {@link Publisher} completes.</li>
     * <li>If the instance of {@code T} emits a failure, the returned {@link Publisher} emits a failure.</li>
     * <li>If the instance of {@code T} emits a {@code null} value, the returned {@link Publisher} emits an
     * {@link NullPointerException} as {@code null} is not a valid value.</li>
     * <li>If the instance of {@code T} does neither emits a value nor a signal, the returned {@code Publisher}
     * does not emits values or signals.</li>
     * <li>This operation is a pass-through for back-pressure and its behavior is determined by the back-pressure
     * behavior of the returned instance.</li>
     * </ul>
     *
     * @param publisher the {@link Publisher} to convert. Must not be {@code null}.
     * @param <X>       the type of data emitted by the passed {@link Publisher}.
     * @return a {@code non-null} instance of {@code T}.
     */
    <X> T fromPublisher(Publisher<X> publisher);

    /**
     * @return the conversion type. Must not be {@code null}. Notice that sub-classes of the returned class are also
     * managed by the same converter.
     */
    Class<T> type();

}
