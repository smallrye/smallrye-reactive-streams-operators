package io.smallrye.reactive.streams.stages;

import java.util.Objects;
import java.util.function.Predicate;

import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;

/**
 * Implementation of {@link Stage.Filter} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FilterStageFactory implements ProcessingStageFactory<Stage.Filter> {

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> ProcessingStage<I, O> create(Engine engine, Stage.Filter stage) {
        Objects.requireNonNull(stage);
        Predicate predicate = Objects.requireNonNull(stage.getPredicate());
        return source -> (Flowable<O>) source.filter(predicate::test);
    }
}
