package io.smallrye.reactive.streams.stages;

import java.util.Objects;
import java.util.function.Function;

import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;

import io.reactivex.Flowable;
import io.reactivex.plugins.RxJavaPlugins;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;
import io.smallrye.reactive.streams.utils.recovery.OnErrorReturn;

/**
 * Implementation of the {@link Stage.OnErrorResume} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class OnErrorResumeStageFactory implements ProcessingStageFactory<Stage.OnErrorResume> {

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> ProcessingStage<I, O> create(Engine engine, Stage.OnErrorResume stage) {
        Function<Throwable, I> function = (Function<Throwable, I>) Objects.requireNonNull(stage).getFunction();
        Objects.requireNonNull(function);
        return source -> (Flowable<O>) RxJavaPlugins.onAssembly(new OnErrorReturn<>(source, function));
    }
}
