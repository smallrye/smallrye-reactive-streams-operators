package io.smallrye.reactive.streams.api.groups;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.tuples.Pair;
import io.smallrye.reactive.streams.api.tuples.Tuples;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Configuresthe combination of 2 {@link Uni unis}.
 *
 * @param <T1> the type of result of the first {@link Uni}
 * @param <T2> the type of result of the second {@link Uni}
 */
public class AndGroup2<T1, T2> extends AndGroupIterable<T1> {


    public AndGroup2(Uni<T1> source, Uni<? extends T2> other) {
        super(source, Collections.singletonList(other), false);
    }

    /**
     * Configure the processing to wait until all the {@link Uni unis} to complete (successfully or not) before
     * propagating the failure. If several failures have been collected, a
     * {@link io.smallrye.reactive.streams.api.CompositeException} is propagated wrapping the different failures.
     *
     * @return the current {@link AndGroup2}
     */
    public AndGroup2<T1, T2> awaitCompletion() {
        super.awaitCompletion();
        return this;
    }

    /**
     * @return the resulting {@link Uni}. The results are combined into a {@link Pair Pair&lt;T1, T2&gt;}.
     */
    public Uni<Pair<T1, T2>> asPair() {
        return combinedWith(Pair::of);
    }

    /**
     * Creates the resulting {@link Uni}. The results are combined using the given combinator function.
     *
     * @param combinator the combinator function, must not be {@code null}
     * @param <O>        the type of result
     * @return the resulting {@link Uni}. The result are combined into a {@link Pair Pair&lt;T1, T2&gt;}.
     */
    @SuppressWarnings("unchecked")
    public <O> Uni<O> combinedWith(BiFunction<T1, T2, O> combinator) {
        Function<List<?>, O> function = list -> {
            Tuples.ensureArity(list, 2);
            T1 item1 = (T1) list.get(0);
            T2 item2 = (T2) list.get(1);
            return combinator.apply(item1, item2);
        };
        return super.combinedWith(function);
    }


}
