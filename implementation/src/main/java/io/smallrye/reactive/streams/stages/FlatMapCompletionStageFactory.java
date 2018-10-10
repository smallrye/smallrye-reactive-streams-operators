package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;
import io.smallrye.reactive.streams.utils.Casts;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static io.smallrye.reactive.streams.utils.CompletionStageToPublisher.fromCompletionStage;

/**
 * Implementation of the {@link Stage.FlatMapCompletionStage} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FlatMapCompletionStageFactory
        implements ProcessingStageFactory<Stage.FlatMapCompletionStage> {

    @Override
    public <I, O> ProcessingStage<I, O> create(Engine engine,
                                               Stage.FlatMapCompletionStage stage) {
        Function<I, CompletionStage<O>> mapper = Casts.cast(
                Objects.requireNonNull(stage).getMapper());
        return new FlatMapCompletionStage<>(mapper);
    }

    private static class FlatMapCompletionStage<I, O> implements ProcessingStage<I, O> {
        private final Function<I, CompletionStage<O>> mapper;

        private FlatMapCompletionStage(Function<I, CompletionStage<O>> mapper) {
            this.mapper = Objects.requireNonNull(mapper);
        }

        @Override
        public Flowable<O> apply(Flowable<I> source) {
            return source.flatMap((I item) -> {
                if (item == null) {
                    // Throw an NPE to be compliant with the reactive stream spec.
                    throw new NullPointerException();
                }
                CompletionStage<O> result = mapper.apply(item);
                if (result == null) {
                    // Throw an NPE to be compliant with the reactive stream spec.
                    throw new NullPointerException();
                }
                return fromCompletionStage(result, false);
            }, 1);
        }
    }


}
