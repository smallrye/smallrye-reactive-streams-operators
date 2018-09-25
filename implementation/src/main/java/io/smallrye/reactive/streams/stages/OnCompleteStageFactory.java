package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;

/**
 * Implementation of the {@link Stage.OnComplete} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class OnCompleteStageFactory implements ProcessingStageFactory<Stage.OnComplete> {

    @SuppressWarnings("unchecked")
    @Override
    public <IN, OUT> ProcessingStage<IN, OUT> create(Engine engine, Stage.OnComplete stage) {
        Runnable runnable = Objects.requireNonNull(stage).getAction();
        Objects.requireNonNull(runnable);
        return source -> (Flowable<OUT>) source.doOnComplete(runnable::run);
    }
}
