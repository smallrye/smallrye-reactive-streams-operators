package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.PublisherStage;
import io.smallrye.reactive.streams.operators.PublisherStageFactory;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.reactivestreams.Publisher;

import java.util.Objects;

/**
 * Implementation of the {@link Stage.PublisherStage} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FromPublisherStageFactory implements PublisherStageFactory<Stage.PublisherStage> {

    @SuppressWarnings("unchecked")
    @Override
    public <O> PublisherStage<O> create(Engine engine, Stage.PublisherStage stage) {
        Publisher<O> publisher = (Publisher<O>) Objects.requireNonNull(Objects.requireNonNull(stage.getRsPublisher()));
        return () -> Flowable.fromPublisher(publisher);
    }
}
