package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;

/**
 * Specialization of the {@link ProcessingStage} for data sources (publishers).
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface PublisherStage<OUT> extends ProcessingStage<Void, OUT> {

    /**
     * @return the publisher.
     */
    Flowable<OUT> create();

    /**
     * Method used to comply with the {@link ProcessingStage} interface. In this case, the source should be {@code null} as
     * it is ignored.
     *
     * @param source the input stream - ignored
     * @return the created publisher
     */
    @Override
    default Flowable<OUT> process(Flowable<Void> source) {
        return create();
    }
}
