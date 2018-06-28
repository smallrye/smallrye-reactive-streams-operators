package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;

/**
 * Implementation of the {@link Stage.Failed} stage. It just returns a flowable marked as failed.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FailedPublisherStageFactory implements PublisherStageFactory<Stage.Failed> {

  @Override
  public <OUT> PublisherStage<OUT> create(Engine engine, Stage.Failed stage) {
    Throwable error = Objects.requireNonNull(Objects.requireNonNull(stage).getError());
    return () -> Flowable.error(error);
  }
}
