package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.ProcessingStage;
import io.smallrye.reactive.streams.operators.ProcessingStageFactory;
import io.smallrye.reactive.streams.utils.CouplingProcessor;
import org.eclipse.microprofile.reactive.streams.spi.Graph;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.eclipse.microprofile.reactive.streams.spi.SubscriberWithCompletionStage;
import org.reactivestreams.Publisher;

import java.util.Objects;

/**
 * Implementation of the {@link Stage.Coupled} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CoupledStageFactory implements ProcessingStageFactory<Stage.Coupled> {
    @Override
    public <I, O> ProcessingStage<I, O> create(Engine engine, Stage.Coupled stage) {
        Graph source = Objects.requireNonNull(stage.getPublisher());
        Graph sink = Objects.requireNonNull(stage.getSubscriber());

        Publisher<O> publisher = engine.buildPublisher(source);
        SubscriberWithCompletionStage<I, ?> subscriber = engine.buildSubscriber(sink);

        return upstream ->
                Flowable.fromPublisher(
                        new CouplingProcessor<>(upstream, subscriber.getSubscriber(), publisher)
                );
    }
}
