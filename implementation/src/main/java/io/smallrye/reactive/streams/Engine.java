package io.smallrye.reactive.streams;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.operators.*;
import io.smallrye.reactive.streams.spi.Transformer;
import io.smallrye.reactive.streams.stages.Stages;
import io.smallrye.reactive.streams.utils.ConnectableProcessor;
import io.smallrye.reactive.streams.utils.DefaultSubscriberWithCompletionStage;
import io.smallrye.reactive.streams.utils.WrappedProcessor;
import org.eclipse.microprofile.reactive.streams.operators.spi.Graph;
import org.eclipse.microprofile.reactive.streams.operators.spi.ReactiveStreamsEngine;
import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;
import org.eclipse.microprofile.reactive.streams.operators.spi.SubscriberWithCompletionStage;
import org.eclipse.microprofile.reactive.streams.operators.spi.UnsupportedStageException;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

import java.util.concurrent.CompletionStage;

public class Engine implements ReactiveStreamsEngine {

    @Override
    public <T> Publisher<T> buildPublisher(Graph graph) {
        Flowable<T> flowable = null;
        for (Stage stage : graph.getStages()) {
            Operator operator = Stages.lookup(stage);
            if (flowable == null) {
                if (operator instanceof PublisherOperator) {
                    flowable = createPublisher(stage, (PublisherOperator) operator);
                } else {
                    throw new IllegalArgumentException("Expecting a publisher stage, got a " + stage);
                }
            } else {
                if (operator instanceof ProcessorOperator) {
                    flowable = applyProcessors(flowable, stage, (ProcessorOperator) operator);
                } else {
                    throw new IllegalArgumentException("Expecting a processor stage, got a " + stage);
                }
            }
        }
        return flowable;
    }


    @Override
    public <T, R> SubscriberWithCompletionStage<T, R> buildSubscriber(Graph graph) {
        Processor<T, T> processor = new ConnectableProcessor<>();
        Flowable<T> flowable = Flowable.fromPublisher(processor);
        for (Stage stage : graph.getStages()) {
            Operator operator = Stages.lookup(stage);
            if (operator instanceof ProcessorOperator) {
                flowable = applyProcessors(flowable, stage, (ProcessorOperator) operator);
            } else if (operator instanceof TerminalOperator) {
                CompletionStage<R> result = applySubscriber(Transformer.apply(flowable), stage,
                        (TerminalOperator) operator);
                return new DefaultSubscriberWithCompletionStage<>(processor, result);
            } else {
                throw new UnsupportedStageException(stage);
            }
        }

        throw new IllegalArgumentException("The graph does not have a valid final stage");
    }

    @Override
    public <T, R> Processor<T, R> buildProcessor(Graph graph) {
        Processor<T, T> processor = new ConnectableProcessor<>();

        Flowable<T> flowable = Flowable.fromPublisher(processor);
        for (Stage stage : graph.getStages()) {
            Operator operator = Stages.lookup(stage);
            flowable = applyProcessors(flowable, stage, (ProcessorOperator) operator);
        }

        //noinspection unchecked
        return (Processor<T, R>) new WrappedProcessor<>(processor, flowable);
    }

    @Override
    public <T> CompletionStage<T> buildCompletion(Graph graph) {
        Flowable<?> flowable = null;
        for (Stage stage : graph.getStages()) {
            Operator operator = Stages.lookup(stage);
            if (operator instanceof PublisherOperator) {
                flowable = createPublisher(stage, (PublisherOperator) operator);
            } else if (operator instanceof ProcessorOperator) {
                flowable = applyProcessors(flowable, stage, (ProcessorOperator) operator);
            } else {
                return applySubscriber(flowable, stage, (TerminalOperator) operator);
            }
        }

        throw new IllegalArgumentException("Graph did not have terminal stage");
    }

    private <I, O> Flowable<O> applyProcessors(Flowable<I> flowable, Stage stage, ProcessorOperator operator) {
        @SuppressWarnings("unchecked") ProcessingStage<I, O> ps = operator.create(this, stage);
        return Transformer.apply(ps.apply(flowable));
    }

    private <T, R> CompletionStage<R> applySubscriber(Flowable<T> flowable, Stage stage, TerminalOperator operator) {
        @SuppressWarnings("unchecked") TerminalStage<T, R> ps = operator.create(this, stage);
        return ps.apply(Transformer.apply(flowable));
    }

    private <O> Flowable<O> createPublisher(Stage stage, PublisherOperator operator) {
        @SuppressWarnings("unchecked") PublisherStage<O> ps = operator.create(this, stage);
        return Transformer.apply(ps.get());
    }

}
