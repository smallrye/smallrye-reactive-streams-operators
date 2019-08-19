package io.smallrye.reactive.streams.operators;

import java.util.function.Function;

import io.reactivex.Flowable;

/**
 * Defines a processing stage - so a stream transformation.
 *
 * @param <I> type of the received items
 * @param <O> type of the emitted items
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@FunctionalInterface
public interface ProcessingStage<I, O> extends Function<Flowable<I>, Flowable<O>> {

    /**
     * Adapts the streams.
     *
     * @param source the input stream, must not be {@code null}
     * @return the adapted stream, must not be {@code null}
     */
    Flowable<O> apply(Flowable<I> source);

}
