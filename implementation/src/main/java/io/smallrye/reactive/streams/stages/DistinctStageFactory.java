package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;
import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;

import java.util.Objects;

/**
 * Implementation of the {@link Stage.Distinct} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DistinctStageFactory implements ProcessingStageFactory<Stage.Distinct> {

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> ProcessingStage<I, O> create(Engine engine, Stage.Distinct stage) {
        Objects.requireNonNull(stage);
        return source -> (Flowable<O>) source.distinct();
    }
}
