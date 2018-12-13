package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;
import io.smallrye.reactive.streams.utils.Casts;
import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Implementation of the {@link Stage.DropWhile} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DropWhileStageFactory implements ProcessingStageFactory<Stage.DropWhile> {

    @Override
    public <I, O> ProcessingStage<I, O> create(Engine engine, Stage.DropWhile stage) {
        Predicate<I> predicate = Casts.cast(stage.getPredicate());
        return Casts.cast(new TakeWhile<>(predicate));
    }

    private static class TakeWhile<I> implements ProcessingStage<I, I> {
        private final Predicate<I> predicate;

        TakeWhile(Predicate<I> predicate) {
            this.predicate = Objects.requireNonNull(predicate);
        }

        @Override
        public Flowable<I> apply(Flowable<I> source) {
            return source.skipWhile(predicate::test);
        }
    }
}
