package io.smallrye.reactive.streams.operators;

import io.reactivex.Flowable;

import java.util.concurrent.CompletionStage;

/**
 * Defines a terminal stage - so a stream subscription and observation.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@FunctionalInterface
public interface TerminalStage<IN, OUT> {

    /**
     * Creates the {@link CompletionStage} called when the embedded logic has completed or failed.
     *
     * @param flowable the observed / subscribed stream
     * @return the asynchronous result
     */
    CompletionStage<OUT> toCompletionStage(Flowable<IN> flowable);

}
