package io.smallrye.reactive.converters;

import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Converts a specific reactive types from and to {@link CompletionStage} and {@code Publisher}.
 * @param <T> the converted type.
 */
public interface ReactiveTypeConverter<T> {

    /**
     * Transforms an instance of {@code T} to a {@link CompletionStage} completed with an {@link Optional}.
     * Each converter instances can use specific rules, however the following set of rules are mandatory:
     *
     * <ul>
     *     <li>The returned {@link CompletionStage} must never be {@code null}.</li>
     *     <li>The returned {@link CompletionStage} completes with the first emitted value wrapped into an
     *     {@link Optional} instance, as a consequence it must never be completed with {@code null}.</li>
     *     <li>If the passed {@code instance} emits several values, only the first one is considered, others are
     *     discarded.</li>
     *     <li>If the passed {@code instance} fails before emitting a value, the returned {@link CompletionStage}
     *     completes with this failure.</li>
     *     <li>If the passed {@code instance} does not emit any value and does not fail or complete, the returned
     *     {@code CompletionStage} does not complete.</li>
     *     <li>If the passed {@code instance} completes <strong>before</strong> emitting a value, the
     *     {@link CompletionStage} is completed with an empty {@link Optional}.</li>
     *     <li>If the passed {@code instance} emits {@code null} as first value (if supported), the
     *     {@link CompletionStage} is completed with an empty {@link Optional}. As a consequence, there are no
     *     differences between an {@code instance} emitting {@code null} as first value or completing without emitting
     *     a value. If the {@code instance} does not support emitting {@code null} values, the returned
     *     {@link CompletionStage} must be completed with a failure.</li>
     * </ul>
     *
     * @param instance the instance to convert to a {@link CompletionStage}. Must not be {@code null}.
     * @param <X> the type wrapped into the resulting {@link Optional} emitted by the return {@link CompletionStage}. It
     *           is generally the type of data emitted by the passed {@code instance}.
     * @return a {@code non-null} {@link CompletionStage}.
     */
    <X> CompletionStage<Optional<X>> toCompletionStage(T instance);

    <X>Publisher<X> toRSPublisher(T instance);

    /**
     *
     * @param cs
     * @param <X>
     * @return
     */
    <X> T fromCompletionStage(CompletionStage<X> cs);

    <X> T fromPublisher(Publisher<X> publisher);

    Class<T> type();

}
