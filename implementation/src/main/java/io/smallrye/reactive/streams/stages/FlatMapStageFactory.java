package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;
import io.smallrye.reactive.streams.utils.Casts;
import io.smallrye.reactive.streams.utils.DelegatingSubscriber;
import org.eclipse.microprofile.reactive.streams.spi.Graph;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
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
    public <IN, OUT> ProcessingStage<IN, OUT> create(Engine engine, Stage.FlatMap stage) {
        Function<IN, Graph> mapper = Casts.cast(stage.getMapper());
        return new FlatMapStage<>(engine, mapper);
    }

    private static class FlatMapStage<IN, OUT> implements ProcessingStage<IN, OUT> {
        private final Engine engine;
        private final Function<IN, Graph> mapper;

        private FlatMapStage(Engine engine, Function<IN, Graph> mapper) {
            this.mapper = Objects.requireNonNull(mapper);
            this.engine = engine;
        }

        @Override
        public Flowable<OUT> process(Flowable<IN> source) {
            return source
                    .concatMap(e -> {
                        Graph graph = mapper.apply(e);
                        Flowable<OUT> publisher = Flowable.fromPublisher(
                                Objects.requireNonNull(engine.buildPublisher(Objects.requireNonNull(graph))));

                        return delegate -> {
                            // Required because RX FlatMap subscriber does not enforce the reactive stream spec.
                            Subscriber<OUT> facade = new DelegatingSubscriber<>(delegate);
                            publisher.subscribe(facade);
                        };

                    });
        }
    }
}
