package io.smallrye.reactive.streams.stages;

import java.util.Objects;

import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.PublisherStage;
import io.smallrye.reactive.streams.operators.PublisherStageFactory;

/**
 * Implementation of the {@link Stage.Failed} stage. It just returns a flowable marked as failed.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FailedPublisherStageFactory implements PublisherStageFactory<Stage.Failed> {

    @Override
    public <O> PublisherStage<O> create(Engine engine, Stage.Failed stage) {
        Throwable error = Objects.requireNonNull(Objects.requireNonNull(stage).getError());
        return () -> Flowable.error(error);
    }
}
