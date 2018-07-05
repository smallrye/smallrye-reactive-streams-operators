package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.utils.Casts;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Implementation of the {@link Stage.DropWhile} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DropWhileStageFactory implements ProcessingStageFactory<Stage.DropWhile> {

  @Override
  public <IN, OUT> ProcessingStage<IN, OUT> create(Engine engine, Stage.DropWhile stage) {
    Predicate<IN> predicate = Casts.cast(stage.getPredicate());
    return Casts.cast(new TakeWhile<>(predicate));
  }

  private static class TakeWhile<IN> implements ProcessingStage<IN, IN> {
    private final Predicate<IN> predicate;

    TakeWhile(Predicate<IN> predicate) {
      this.predicate = Objects.requireNonNull(predicate);
    }

    @Override
    public Flowable<IN> process(Flowable<IN> source) {
      return source.skipWhile(predicate::test);
    }
  }
}
