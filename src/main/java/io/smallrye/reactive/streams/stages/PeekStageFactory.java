package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Implementation of the {@link Stage.Peek} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PeekStageFactory implements ProcessingStageFactory<Stage.Peek> {

  @SuppressWarnings("unchecked")
  @Override
  public <IN, OUT> ProcessingStage<IN, OUT> create(Engine engine, Stage.Peek stage) {
    Consumer<IN> consumer = (Consumer<IN>) Objects.requireNonNull(stage)
        .getConsumer();
    Objects.requireNonNull(consumer);
    return source -> (Flowable<OUT>) source.doOnNext(consumer::accept);
  }
}
