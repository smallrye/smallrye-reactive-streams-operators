package io.smallrye.reactive.streams.stages;

import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.utils.Casts;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.function.Function;

/**
 * Implementation of the {@link Stage.Map} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MapStageFactory implements ProcessingStageFactory<Stage.Map> {

  @Override
  public <IN, OUT> ProcessingStage<IN, OUT> create(Engine engine, Stage.Map stage) {
    Function<IN, OUT> mapper = Casts.cast(stage.getMapper());
    Objects.requireNonNull(mapper);
    return source -> source.map(mapper::apply);
  }
}
