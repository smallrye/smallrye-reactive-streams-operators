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

  @Override
  public <IN, OUT> ProcessingStage<IN, OUT> create(Engine engine, Stage.TakeWhile stage) {
    Predicate<IN> predicate = Casts.cast(stage.getPredicate().get());
    return Casts.cast(new TakeWhile<>(predicate, stage.isInclusive()));
  }

  private static class TakeWhile<IN> implements ProcessingStage<IN, IN> {
    private final Predicate<IN> predicate;
    private final boolean includeLast;

    TakeWhile(Predicate<IN> predicate, boolean inclusive) {
      this.predicate = Objects.requireNonNull(predicate);
      this.includeLast = inclusive;
    }

    @Override
    public Flowable<IN> process(Flowable<IN> source) {
      if (includeLast) {
        return source.takeUntil(item -> !predicate.test(item));
      } else {
        return source.takeWhile(predicate::test);
      }
    }
  }
}
