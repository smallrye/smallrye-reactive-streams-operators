package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Implementation of the {@link Stage.OnError} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class OnErrorStageFactory implements ProcessingStageFactory<Stage.OnError> {

    @SuppressWarnings("unchecked")
    @Override
    public <IN, OUT> ProcessingStage<IN, OUT> create(Engine engine, Stage.OnError stage) {
        Consumer<Throwable> consumer = Objects.requireNonNull(stage).getConsumer();
        Objects.requireNonNull(consumer);
        return source -> (Flowable<OUT>) source.doOnError(consumer::accept);
    }
}
