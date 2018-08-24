package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;

/**
 * Implementation of the {@link Stage.Of} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FromIterableStageFactory implements PublisherStageFactory<Stage.Of> {

  @SuppressWarnings("unchecked")
  @Override
  public <OUT> PublisherStage<OUT> create(Engine engine, Stage.Of stage) {
    Iterable<OUT> elements = (Iterable<OUT>) Objects.requireNonNull(Objects.requireNonNull(stage).getElements());
    return () -> Flowable.fromIterable(elements);
  }
}
