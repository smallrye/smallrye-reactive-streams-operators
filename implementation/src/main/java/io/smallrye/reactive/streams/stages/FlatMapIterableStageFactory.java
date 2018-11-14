package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;
import io.smallrye.reactive.streams.utils.Casts;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FlatMapIterableStageFactory implements ProcessingStageFactory<Stage.FlatMapIterable> {

    @Override
    public <I, O> ProcessingStage<I, O> create(Engine engine, Stage.FlatMapIterable stage) {
        Function<I, Iterable<O>> mapper = Casts.cast(stage.getMapper());
        return new FlatMapIterable<>(mapper);
    }

    private static class FlatMapIterable<I, O> implements ProcessingStage<I, O> {
        private final Function<I, Iterable<O>> mapper;

        private FlatMapIterable(Function<I, Iterable<O>> mapper) {
            this.mapper = Objects.requireNonNull(mapper);
        }

        @Override
        public Flowable<O> apply(Flowable<I> source) {
            return source.concatMapIterable(mapper::apply);
        }
    }

}
