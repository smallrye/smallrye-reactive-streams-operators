package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;

/**
 * Implementation of the {@link Stage.Distinct} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DistinctStageFactory implements ProcessingStageFactory<Stage.Distinct> {

    @SuppressWarnings("unchecked")
    @Override
    public <IN, OUT> ProcessingStage<IN, OUT> create(Engine engine, Stage.Distinct stage) {
        Objects.requireNonNull(stage);
        return source -> (Flowable<OUT>) source.distinct();
    }
}
