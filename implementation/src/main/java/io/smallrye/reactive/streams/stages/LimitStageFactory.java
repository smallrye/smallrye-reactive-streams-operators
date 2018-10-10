package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

/**
 * Implementation of the {@link Stage.Limit} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class LimitStageFactory implements ProcessingStageFactory<Stage.Limit> {

    @Override
    public <IN, OUT> ProcessingStage<IN, OUT> create(Engine engine, Stage.Limit stage) {
        long limit = stage.getLimit();
        return source -> (Flowable<OUT>) source.limit(limit);
    }
}
