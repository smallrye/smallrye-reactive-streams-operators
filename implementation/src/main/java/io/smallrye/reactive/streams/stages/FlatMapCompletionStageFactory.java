package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.processors.AsyncProcessor;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.utils.Casts;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * Implementation of the {@link Stage.FlatMapCompletionStage} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FlatMapCompletionStageFactory
  implements ProcessingStageFactory<Stage.FlatMapCompletionStage> {

  @Override
  public <IN, OUT> ProcessingStage<IN, OUT> create(Engine engine,
                                                   Stage.FlatMapCompletionStage stage) {
    Function<IN, CompletionStage<OUT>> mapper = Casts.cast(
      Objects.requireNonNull(stage).getMapper());
    return new FlatMapCompletionStage<>(mapper);
  }

  private static class FlatMapCompletionStage<IN, OUT> implements ProcessingStage<IN, OUT> {
    private final Function<IN, CompletionStage<OUT>> mapper;

    private FlatMapCompletionStage(Function<IN, CompletionStage<OUT>> mapper) {
      this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public Flowable<OUT> process(Flowable<IN> source) {
      return source.concatMap(e -> {
        if (e == null) {
          throw new NullPointerException();
        }
        CompletionStage<OUT> result = mapper.apply(e);
        if (result == null) {
          throw new NullPointerException();
        }
        return fromCompletionStage(result);
      }, 1);
    }
  }

  /**
   * Returns a {@link Flowable} that emits the value of the given {@link CompletionStage}, its error or
   * @{code NullPointerException} if it signals {@code null}.
   *
   * @param <T> the value type
   * @param future the source {@link CompletionStage} instance
   * @return the new {@link Flowable} instance
   */
  private static <T> Flowable<T> fromCompletionStage(CompletionStage<T> future) {
    AsyncProcessor<T> processor = AsyncProcessor.create();

    future.whenComplete((v, e) -> {
      if (e != null) {
        processor.onError(e);
      } else
      if (v != null) {
        processor.onNext(v);
        processor.onComplete();
      } else {
        processor.onError(new NullPointerException());
      }
    });

    return processor;
  }

}
