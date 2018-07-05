package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;

/**
 * Implementation of the {@link Stage.OnTerminate} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class OnTerminateStageFactory implements ProcessingStageFactory<Stage.OnTerminate> {

  @SuppressWarnings("unchecked")
  @Override
  public <IN, OUT> ProcessingStage<IN, OUT> create(Engine engine, Stage.OnTerminate stage) {
    Runnable runnable = Objects.requireNonNull(stage).getAction();
    Objects.requireNonNull(runnable);
    // Interesting issue when using onTerminate, the TCK fails because the issue is reported twice
    // First, the onComplete "part" is called, throws an exception, and then call the doOnError part
    // which throws another exception. 
    return source -> (Flowable<OUT>) source.doOnError(t -> runnable.run()).doOnComplete(runnable::run);
  }
}
