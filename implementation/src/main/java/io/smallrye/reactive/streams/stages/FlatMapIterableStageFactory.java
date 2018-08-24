package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.utils.Casts;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FlatMapIterableStageFactory implements ProcessingStageFactory<Stage.FlatMapIterable> {

  @Override
  public <IN, OUT> ProcessingStage<IN, OUT> create(Engine engine, Stage.FlatMapIterable stage) {
    Function<IN, Iterable<OUT>> mapper = Casts.cast(stage.getMapper());
    return new FlatMapIterable<>(mapper);
  }

  private static class FlatMapIterable<IN, OUT> implements ProcessingStage<IN, OUT> {
    private final Function<IN, Iterable<OUT>> mapper;

    private FlatMapIterable(Function<IN, Iterable<OUT>> mapper) {
      this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public Flowable<OUT> process(Flowable<IN> source) {
      return source.concatMapIterable(mapper::apply);
    }
  }

}
