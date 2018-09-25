package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.plugins.RxJavaPlugins;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.utils.FlowableCollector;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collector;

/**
 * Implement the {@link Stage.Collect} stage. It accumulates the result in a {@link Collector} and
 * redeems the last result.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CollectStageFactory implements TerminalStageFactory<Stage.Collect> {


    @SuppressWarnings("unchecked")
    @Override
    public <IN, OUT> TerminalStage<IN, OUT> create(Engine engine, Stage.Collect stage) {
        Collector<IN, Object, OUT> collector = (Collector<IN, Object, OUT>) Objects.requireNonNull(stage).getCollector();
        Objects.requireNonNull(collector);
        return new CollectStage<>(collector);
    }

    private static class CollectStage<IN, OUT> implements TerminalStage<IN, OUT> {

        private final Collector<IN, Object, OUT> collector;

        CollectStage(Collector<IN, Object, OUT> collector) {
            this.collector = collector;
        }

        @Override
        public CompletionStage<OUT> toCompletionStage(Flowable<IN> source) {
            CompletableFuture<OUT> future = new CompletableFuture<>();
            Flowable<OUT> flow = source.compose(f -> RxJavaPlugins.onAssembly(new FlowableCollector<>(f, collector)));
            //noinspection ResultOfMethodCallIgnored
            flow
                    .firstElement()
                    .subscribe(
                            future::complete,
                            future::completeExceptionally
                    );
            return future;
        }
    }

}
