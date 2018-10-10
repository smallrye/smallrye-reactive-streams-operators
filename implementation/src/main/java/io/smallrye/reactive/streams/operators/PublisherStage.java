package io.smallrye.reactive.streams.operators;

import io.reactivex.Flowable;

import java.util.function.Supplier;

/**
 * Specialization of the {@link ProcessingStage} for data sources (publishers).
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface PublisherStage<O> extends Supplier<Flowable<O>> {

    /**
     * @return the publisher.
     */
    Flowable<O> get();
}
