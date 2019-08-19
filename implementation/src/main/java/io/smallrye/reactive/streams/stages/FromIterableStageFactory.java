package io.smallrye.reactive.streams.stages;

import java.util.Objects;

import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.PublisherStage;
import io.smallrye.reactive.streams.operators.PublisherStageFactory;

/**
 * Implementation of the {@link Stage.Of} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FromIterableStageFactory implements PublisherStageFactory<Stage.Of> {

    @SuppressWarnings("unchecked")
    @Override
    public <O> PublisherStage<O> create(Engine engine, Stage.Of stage) {
        Iterable<O> elements = (Iterable<O>) Objects.requireNonNull(Objects.requireNonNull(stage).getElements());
        return () -> Flowable.fromIterable(elements);
    }
}
