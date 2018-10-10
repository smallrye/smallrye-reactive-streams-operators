package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

/**
 * Implementation of the {@link Stage.Skip} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class SkipStageFactory implements ProcessingStageFactory<Stage.Skip> {

    @Override
    public <IN, OUT> ProcessingStage<IN, OUT> create(Engine engine, Stage.Skip stage) {
        long skip = stage.getSkip();
        return source -> (Flowable<OUT>) source.skip(skip);
    }
}
