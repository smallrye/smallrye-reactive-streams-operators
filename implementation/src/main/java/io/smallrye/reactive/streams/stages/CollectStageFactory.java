package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.plugins.RxJavaPlugins;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.TerminalStage;
import io.smallrye.reactive.streams.operators.TerminalStageFactory;
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
    public <I, O> TerminalStage<I, O> create(Engine engine, Stage.Collect stage) {
        Collector<I, Object, O> collector = (Collector<I, Object, O>)
                Objects.requireNonNull(stage).getCollector();
        Objects.requireNonNull(collector);
        return new CollectStage<>(collector);
    }

    private static class CollectStage<I, O> implements TerminalStage<I, O> {

        private final Collector<I, Object, O> collector;

        CollectStage(Collector<I, Object, O> collector) {
            this.collector = collector;
        }

        @Override
        public CompletionStage<O> apply(Flowable<I> source) {
            CompletableFuture<O> future = new CompletableFuture<>();
            Flowable<O> flow = source.compose(f -> RxJavaPlugins.onAssembly(new FlowableCollector<>(f, collector)));
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
