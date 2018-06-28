package io.smallrye.reactive.streams.stages;

import io.smallrye.reactive.streams.Engine;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of {@link Stage.Cancel}. It subscribes and disposes the stream immediately.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CancelStageFactory implements TerminalStageFactory<Stage.Cancel> {

  @Override
  public <IN, OUT> TerminalStage<IN, OUT> create(Engine engine, Stage.Cancel stage) {
    Objects.requireNonNull(stage);
    return flowable -> {
      flowable.subscribe().dispose();
      return CompletableFuture.completedFuture(null);
    };
  }
}
