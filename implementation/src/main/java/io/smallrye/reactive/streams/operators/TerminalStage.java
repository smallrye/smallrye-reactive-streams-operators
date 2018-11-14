package io.smallrye.reactive.streams.operators;

import io.reactivex.Flowable;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * Defines a terminal stage - so a stream subscription and observation.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@FunctionalInterface
public interface TerminalStage<I, O> extends Function<Flowable<I>, CompletionStage<O>> {

    /**
     * Creates the {@link CompletionStage} called when the embedded logic has completed or failed.
     *
     * @param flowable the observed / subscribed stream
     * @return the asynchronous result
     */
    CompletionStage<O> apply(Flowable<I> flowable);

}
