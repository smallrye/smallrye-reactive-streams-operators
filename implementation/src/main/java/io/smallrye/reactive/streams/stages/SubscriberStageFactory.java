package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.TerminalStage;
import io.smallrye.reactive.streams.operators.TerminalStageFactory;
import io.smallrye.reactive.streams.utils.WrappedSubscriber;
import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;
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
    public <I, O> TerminalStage<I, O> create(Engine engine, Stage.SubscriberStage stage) {
        Subscriber<I> subscriber = (Subscriber<I>) Objects.requireNonNull(stage).getRsSubscriber();
        Objects.requireNonNull(subscriber);
        return (TerminalStage<I, O>) new SubscriberStage<>(subscriber);
    }

    private static class SubscriberStage<I> implements TerminalStage<I, Void> {

        private final Subscriber<I> subscriber;

        SubscriberStage(Subscriber<I> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public CompletionStage<Void> apply(Flowable<I> source) {
            WrappedSubscriber<I> wrapped = new WrappedSubscriber<>(subscriber);
            source.safeSubscribe(wrapped);
            return wrapped.future();
        }
    }

}
