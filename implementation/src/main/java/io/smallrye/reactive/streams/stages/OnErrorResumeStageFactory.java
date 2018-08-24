package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.internal.operators.flowable.FlowableOnErrorReturn;
import io.reactivex.plugins.RxJavaPlugins;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.utils.recovery.OnErrorResumeWith;
import io.smallrye.reactive.streams.utils.recovery.OnErrorReturn;
import org.eclipse.microprofile.reactive.streams.spi.Graph;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.function.Function;

/**
 * Implementation of the {@link Stage.OnErrorResume} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class OnErrorResumeStageFactory implements ProcessingStageFactory<Stage.OnErrorResume> {

  @SuppressWarnings("unchecked")
  @Override
  public <IN, OUT> ProcessingStage<IN, OUT> create(Engine engine, Stage.OnErrorResume stage) {
    Function<Throwable, IN> function = (Function<Throwable, IN>) Objects.requireNonNull(stage).getFunction();
    Objects.requireNonNull(function);
    return source -> (Flowable<OUT>) RxJavaPlugins.onAssembly(new OnErrorReturn<>(source, function));
  }
}
