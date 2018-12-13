package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;
import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Implementation of the {@link Stage.TakeWhile} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class TakeWhileStageFactory implements ProcessingStageFactory<Stage.TakeWhile> {

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> ProcessingStage<I, O> create(Engine engine, Stage.TakeWhile stage) {
        Predicate<I> predicate = (Predicate<I>) Objects.requireNonNull(stage.getPredicate());
        return source -> (Flowable<O>) source.takeWhile(predicate::test);
    }
}
