package io.smallrye.reactive.streams.stages;

import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.TerminalStage;
import io.smallrye.reactive.streams.operators.TerminalStageFactory;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the {@link Stage.FindFirst} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FindFirstStageFactory implements TerminalStageFactory<Stage.FindFirst> {

    private final static TerminalStage<?, Optional<?>> INSTANCE
            = (TerminalStage<?, Optional<?>>) source -> {
        CompletableFuture<Optional<?>> future = new CompletableFuture<>();
        //noinspection ResultOfMethodCallIgnored
        source.map(Optional::of).first(Optional.empty())
                .subscribe(
                        future::complete, future::completeExceptionally
                );
        return future;
    };

    @SuppressWarnings("unchecked")
    @Override
    public <IN, OUT> TerminalStage<IN, OUT> create(Engine engine, Stage.FindFirst stage) {
        Objects.requireNonNull(stage); // Not really useful here as it conveys no parameters, so just here for symmetry
        return (TerminalStage<IN, OUT>) INSTANCE;
    }

}
