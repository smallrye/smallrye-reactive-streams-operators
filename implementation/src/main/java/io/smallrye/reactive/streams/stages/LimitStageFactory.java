package io.smallrye.reactive.streams.stages;

import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;

/**
 * Implementation of the {@link Stage.Limit} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class LimitStageFactory implements ProcessingStageFactory<Stage.Limit> {

    @Override
    public <I, O> ProcessingStage<I, O> create(Engine engine, Stage.Limit stage) {
        long limit = stage.getLimit();
        return source -> (Flowable<O>) source.limit(limit);
    }
}
