package io.smallrye.reactive.streams.operators;

import io.smallrye.reactive.streams.Engine;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

/**
 * Factory to create {@link TerminalStage} instances.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@FunctionalInterface
public interface TerminalStageFactory<T extends Stage> {

    /**
     * Creates the instance.
     *
     * @param engine the reactive engine, must not be {@code null}
     * @param stage  the stage, must not be {@code null}
     * @param <IN>   incoming data
     * @param <OUT>  computed result
     * @return the terminal stage, must not be {@code null}
     */
    <IN, OUT> TerminalStage<IN, OUT> create(Engine engine, T stage);

}
