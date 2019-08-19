package io.smallrye.reactive.streams.stages;

import java.util.Objects;

import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;

/**
 * Implementation of the {@link Stage.OnComplete} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class OnCompleteStageFactory implements ProcessingStageFactory<Stage.OnComplete> {

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> ProcessingStage<I, O> create(Engine engine, Stage.OnComplete stage) {
        Runnable runnable = Objects.requireNonNull(stage).getAction();
        Objects.requireNonNull(runnable);
        return source -> (Flowable<O>) source.doOnComplete(runnable::run);
    }
}
