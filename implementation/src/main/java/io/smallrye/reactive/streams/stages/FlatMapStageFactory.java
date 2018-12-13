package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;
import io.smallrye.reactive.streams.utils.Casts;
import io.smallrye.reactive.streams.utils.DelegatingSubscriber;
import org.eclipse.microprofile.reactive.streams.operators.spi.Graph;
import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;
import org.reactivestreams.Subscriber;

import java.util.Objects;
import java.util.function.Function;

/**
 * Implementation of the {@link Stage.FlatMap} stage. Be aware it behaves as a RX `concatMap`.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FlatMapStageFactory implements ProcessingStageFactory<Stage.FlatMap> {

    @Override
    public <I, O> ProcessingStage<I, O> create(Engine engine, Stage.FlatMap stage) {
        Function<I, Graph> mapper = Casts.cast(stage.getMapper());
        return new FlatMapStage<>(engine, mapper);
    }

    private static class FlatMapStage<I, O> implements ProcessingStage<I, O> {
        private final Engine engine;
        private final Function<I, Graph> mapper;

        private FlatMapStage(Engine engine, Function<I, Graph> mapper) {
            this.mapper = Objects.requireNonNull(mapper);
            this.engine = engine;
        }

        @Override
        public Flowable<O> apply(Flowable<I> source) {
            return source
                    .concatMap((I item) -> {
                        Graph graph = mapper.apply(item);
                        Flowable<O> publisher = Flowable.fromPublisher(
                                Objects.requireNonNull(engine.buildPublisher(Objects.requireNonNull(graph))));

                        return (Subscriber<? super O> delegate) -> {
                            // Required because RX FlatMap subscriber does not enforce the reactive stream spec.
                            Subscriber<O> facade = new DelegatingSubscriber<>(delegate);
                            publisher.subscribe(facade);
                        };

                    });
        }
    }
}
