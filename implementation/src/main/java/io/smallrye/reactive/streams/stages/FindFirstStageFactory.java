package io.smallrye.reactive.streams.stages;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;

import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.TerminalStage;
import io.smallrye.reactive.streams.operators.TerminalStageFactory;

/**
 * Implementation of the {@link Stage.FindFirst} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FindFirstStageFactory implements TerminalStageFactory<Stage.FindFirst> {

    private static final TerminalStage<?, Optional<?>> INSTANCE = source -> {
        CompletableFuture<Optional<?>> future = new CompletableFuture<>();
        //noinspection ResultOfMethodCallIgnored
        source.map(Optional::of).first(Optional.empty())
                .subscribe(
                        future::complete, future::completeExceptionally);
        return future;
    };

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> TerminalStage<I, O> create(Engine engine, Stage.FindFirst stage) {
        Objects.requireNonNull(stage); // Not really useful here as it conveys no parameters, so just here for symmetry
        return (TerminalStage<I, O>) INSTANCE;
    }

}
