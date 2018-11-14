package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Implementation of the {@link Stage.Peek} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PeekStageFactory implements ProcessingStageFactory<Stage.Peek> {

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> ProcessingStage<I, O> create(Engine engine, Stage.Peek stage) {
        Consumer<I> consumer = (Consumer<I>) Objects.requireNonNull(stage)
                .getConsumer();
        Objects.requireNonNull(consumer);
        return source -> (Flowable<O>) source.doOnNext(consumer::accept);
    }
}
