package io.smallrye.reactive.streams.api.groups;

import io.smallrye.reactive.streams.api.Uni;

import java.util.Arrays;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

/**
 * Combines several {@link Uni} into a new {@link Uni} that will be fulfilled when all {@link Uni} are
 * resolved successfully aggregating their values into a {@link io.smallrye.reactive.streams.api.tuples.Tuple},
 * or using a combinator function.
 * <p>
 * The produced {@link Uni} forwards the failure if one of the two {@link Uni Unis} produces a failure. This will
 * cause the other {@link Uni} to be cancelled, expect if {@code awaitCompletion()} is invoked, which delay the failure
 * propagation when all {@link Uni}s have completed or failed.
 */
public class AndGroup<T1> {

    private final Uni<T1> source;

    public AndGroup(Uni<T1> source) {
        this.source = nonNull(source, "source");
    }

    /**
     * Combines the current {@link Uni} with the given one.
     * Once both {@link Uni} have completed successfully, the result can be retrieved as a
     * {@link io.smallrye.reactive.streams.api.tuples.Pair} or computed using a {@link java.util.function.BiFunction}.
     * <p>
     * The produced {@link Uni} forwards the failure if one of the two {@link Uni Unis} produces a failure. This will
     * cause the other {@link Uni} to be cancelled, expect if {@link AndGroup2#awaitCompletion()} is invoked  which
     * delay the failure propagation until all {@link Uni}s have completed or failed. Note that if both {@link Uni}s
     * failed, the propagated failure is a {@link io.smallrye.reactive.streams.api.CompositeException} wrapping both
     * failures.
     *
     * @param other the other uni, must not be {@code null}, must not be {@code null}
     * @param <T2>  the type of the result for the second uni
     * @return an {@link AndGroup2} to configure the combination
     */
    public <T2> AndGroup2<T1, T2> uni(Uni<? extends T2> other) {
        return new AndGroup2<>(source, other);
    }

    /**
     * Combines the current {@link Uni} with the given ones.
     * Once all {@link Uni} have completed successfully, the result can be retrieved as a
     * {@link io.smallrye.reactive.streams.api.tuples.Tuple3} or computed using a
     * {@link io.smallrye.reactive.streams.api.tuples.Functions.Function3}.
     * <p>
     * The produced {@link Uni} forwards the failure if one of the {@link Uni Unis} produces a failure. This will
     * cause the other {@link Uni} to be cancelled, expect if {@link AndGroup3#awaitCompletion()} is invoked  which
     * delay the failure propagation until all {@link Uni}s have completed or failed. Note that if several {@link Uni}s
     * failed, the propagated failure is a {@link io.smallrye.reactive.streams.api.CompositeException} wrapping all the
     * failures.
     *
     * @param u2   the second uni to be combined, must not be {@code null}
     * @param u3   the third uni to be combined, must not be {@code null}
     * @param <T2> the type of the result for the second uni
     * @param <T3> the type of the result for the third uni
     * @return an {@link AndGroup3} to configure the combination
     */
    public <T2, T3> AndGroup3<T1, T2, T3> unis(Uni<? extends T2> u2, Uni<? extends T3> u3) {
        return new AndGroup3<>(source, u2, u3);
    }

    /**
     * Combines the current {@link Uni} with the given ones.
     * Once all {@link Uni} have completed successfully, the result can be retrieved as a
     * {@link io.smallrye.reactive.streams.api.tuples.Tuple4} or computed using a
     * {@link io.smallrye.reactive.streams.api.tuples.Functions.Function4}.
     * <p>
     * The produced {@link Uni} forwards the failure if one of the {@link Uni Unis} produces a failure. This will
     * cause the other {@link Uni} to be cancelled, expect if {@link AndGroup4#awaitCompletion()} is invoked  which
     * delay the failure propagation until all {@link Uni}s have completed or failed. Note that if several {@link Uni}s
     * failed, the propagated failure is a {@link io.smallrye.reactive.streams.api.CompositeException} wrapping all the
     * failures.
     *
     * @param u2   the second uni to be combined, must not be {@code null}
     * @param u3   the third uni to be combined, must not be {@code null}
     * @param u4   the fourth uni to be combined, must not be {@code null}
     * @param <T2> the type of the result for the second uni
     * @param <T3> the type of the result for the third uni
     * @param <T4> the type of the result for the fourth uni
     * @return an {@link AndGroup4} to configure the combination
     */
    public <T2, T3, T4> AndGroup4<T1, T2, T3, T4> unis(Uni<? extends T2> u2, Uni<? extends T3> u3, Uni<? extends T4> u4) {
        return new AndGroup4<>(source, u2, u3, u4);
    }

    /**
     * Combines the current {@link Uni} with the given ones.
     * Once all {@link Uni} have completed successfully, the result can be retrieved as a
     * {@link io.smallrye.reactive.streams.api.tuples.Tuple5} or computed using a
     * {@link io.smallrye.reactive.streams.api.tuples.Functions.Function5}.
     * <p>
     * The produced {@link Uni} forwards the failure if one of the {@link Uni Unis} produces a failure. This will
     * cause the other {@link Uni} to be cancelled, expect if {@link AndGroup5#awaitCompletion()} is invoked  which
     * delay the failure propagation until all {@link Uni}s have completed or failed. Note that if several {@link Uni}s
     * failed, the propagated failure is a {@link io.smallrye.reactive.streams.api.CompositeException} wrapping all the
     * failures.
     *
     * @param u2   the second uni to be combined, must not be {@code null}
     * @param u3   the third uni to be combined, must not be {@code null}
     * @param u4   the fourth uni to be combined, must not be {@code null}
     * @param u5   the fifth uni to be combined, must not be {@code null}
     * @param <T2> the type of the result for the second uni
     * @param <T3> the type of the result for the third uni
     * @param <T4> the type of the result for the fourth uni
     * @param <T5> the type of the result for the fifth uni
     * @return an {@link AndGroup3} to configure the combination
     */
    public <T2, T3, T4, T5> AndGroup5<T1, T2, T3, T4, T5> unis(Uni<? extends T2> u2, Uni<? extends T3> u3, Uni<? extends T4> u4, Uni<? extends T5> u5) {
        return new AndGroup5<>(source, u2, u3, u4, u5);
    }

    /**
     * Combines the current {@link Uni} with the given ones.
     * Once all {@link Uni} have completed successfully, the result is computed using a {@code combinator} function
     * <p>
     * The produced {@link Uni} forwards the failure if one of the {@link Uni Unis} produces a failure. This will
     * cause the other {@link Uni} to be cancelled, expect if {@link AndGroupIterable#awaitCompletion()} is invoked  which
     * delay the failure propagation until all {@link Uni}s have completed or failed. Note that if several {@link Uni}s
     * failed, the propagated failure is a {@link io.smallrye.reactive.streams.api.CompositeException} wrapping all the
     * failures.
     *
     * @param unis the list of unis, must not be {@code null}, must not contain {@code null}, must not be empty
     * @return an {@link AndGroupIterable} to configure the combination
     */
    public AndGroupIterable unis(Uni<?>... unis) {
        return unis(Arrays.asList(unis));
    }

    /**
     * Combines the current {@link Uni} with the given ones.
     * Once all {@link Uni} have completed successfully, the result is computed using a {@code combinator} function
     * <p>
     * The produced {@link Uni} forwards the failure if one of the {@link Uni Unis} produces a failure. This will
     * cause the other {@link Uni} to be cancelled, expect if {@link AndGroupIterable#awaitCompletion()} is invoked  which
     * delay the failure propagation until all {@link Uni}s have completed or failed. Note that if several {@link Uni}s
     * failed, the propagated failure is a {@link io.smallrye.reactive.streams.api.CompositeException} wrapping all the
     * failures.
     *
     * @param unis the list of unis, must not be {@code null}, must not contain {@code null}, must not be empty
     * @return an {@link AndGroupIterable} to configure the combination
     */
    public AndGroupIterable unis(Iterable<? extends Uni<?>> unis) {
        return new AndGroupIterable<>(source, unis);
    }


}
