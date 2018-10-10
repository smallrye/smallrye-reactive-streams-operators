package io.smallrye.reactive.streams.operators;

import io.reactivex.Flowable;

/**
 * Defines a processing stage - so a stream transformation.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@FunctionalInterface
public interface ProcessingStage<IN, OUT> {

    /**
     * Adapts the streams.
     *
     * @param source the input stream, must not be {@code null}
     * @return the adapted stream, must not be {@code null}
     */
    Flowable<OUT> process(Flowable<IN> source);

}
