package io.smallrye.reactive.streams.stages;

import io.smallrye.reactive.streams.Engine;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

/**
 * Factory to create {@link PublisherStage} instances.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@FunctionalInterface
public interface PublisherStageFactory<T extends Stage> {

  /**
   * Creates the instance.
   *
   * @param engine the reactive engine
   * @param stage  the stage
   * @param <OUT>  output data
   * @return the created processing stage, should never be {@code null}
   */
  <OUT> PublisherStage<OUT> create(Engine engine, T stage);

}
