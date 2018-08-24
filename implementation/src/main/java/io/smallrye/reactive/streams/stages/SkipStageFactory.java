package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.utils.Casts;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Implementation of the {@link Stage.Skip} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class SkipStageFactory implements ProcessingStageFactory<Stage.Skip> {

  @Override
  public <IN, OUT> ProcessingStage<IN, OUT> create(Engine engine, Stage.Skip stage) {
    long skip = stage.getSkip();
    return source -> (Flowable<OUT>) source.skip(skip);
  }
}
