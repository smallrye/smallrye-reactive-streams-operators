package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.TerminalStage;
import io.smallrye.reactive.streams.operators.TerminalStageFactory;
import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of {@link Stage.Cancel}. It subscribes and disposes the stream immediately.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CancelStageFactory implements TerminalStageFactory<Stage.Cancel> {

    @Override
    public <I, O> TerminalStage<I, O> create(Engine engine, Stage.Cancel stage) {
        Objects.requireNonNull(stage);
        return (Flowable<I> flow) -> {
            flow.subscribe(new Subscriber<I>() {

                @Override
                public void onSubscribe(Subscription s) {
                    s.cancel();
                }

                @Override
                public void onNext(I in) {
                    // Do nothing.
                }

                @Override
                public void onError(Throwable t) {
                    // Do nothing.
                }

                @Override
                public void onComplete() {
                    // Do nothing.
                }
            });
            return CompletableFuture.completedFuture(null);
        };
    }
}
