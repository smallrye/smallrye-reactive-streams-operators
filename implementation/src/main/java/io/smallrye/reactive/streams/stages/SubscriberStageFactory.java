package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.utils.WrappedSubscriber;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.reactivestreams.Subscriber;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * Implementation of the {@link Stage.SubscriberStage} stage.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class SubscriberStageFactory implements TerminalStageFactory<Stage.SubscriberStage> {

    @SuppressWarnings("unchecked")
    @Override
    public <IN, OUT> TerminalStage<IN, OUT> create(Engine engine, Stage.SubscriberStage stage) {
        Subscriber<IN> subscriber = (Subscriber<IN>) Objects.requireNonNull(stage).getRsSubscriber();
        Objects.requireNonNull(subscriber);
        return (TerminalStage<IN, OUT>) new SubscriberStage<>(subscriber);
    }

    private static class SubscriberStage<IN> implements TerminalStage<IN, Void> {

        private final Subscriber<IN> subscriber;

        SubscriberStage(Subscriber<IN> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public CompletionStage<Void> toCompletionStage(Flowable<IN> source) {
            WrappedSubscriber<IN> wrapped = new WrappedSubscriber<>(subscriber);
            source.safeSubscribe(wrapped);
            return wrapped.future();
        }
    }

}
