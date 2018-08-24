package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.reactivestreams.Publisher;

import java.util.Objects;

/**
 * Implementation of the {@link Stage.PublisherStage} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FromPublisherStageFactory implements PublisherStageFactory<Stage.PublisherStage> {

  @SuppressWarnings("unchecked")
  @Override
  public <OUT> PublisherStage<OUT> create(Engine engine, Stage.PublisherStage stage) {
    Publisher<OUT> publisher = (Publisher<OUT>) Objects.requireNonNull(Objects.requireNonNull(stage.getRsPublisher()));
    return () -> Flowable.fromPublisher(publisher);
  }
}
