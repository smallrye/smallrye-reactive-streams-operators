package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.utils.Casts;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Implementation of the {@link Stage.TakeWhile} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class TakeWhileStageFactory implements ProcessingStageFactory<Stage.TakeWhile> {

  @SuppressWarnings("unchecked")
  @Override
  public <IN, OUT> ProcessingStage<IN, OUT> create(Engine engine, Stage.TakeWhile stage) {
    Predicate<IN> predicate = (Predicate<IN>) Objects.requireNonNull(stage.getPredicate());
    return source -> (Flowable<OUT>) source.takeWhile(predicate::test);
  }
}
