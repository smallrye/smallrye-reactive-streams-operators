package io.smallrye.reactive.streams;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.operators.*;
import io.smallrye.reactive.streams.spi.ExecutionModel;
import io.smallrye.reactive.streams.stages.*;
import io.smallrye.reactive.streams.utils.ConnectableProcessor;
import io.smallrye.reactive.streams.utils.WrappedProcessor;
import org.eclipse.microprofile.reactive.streams.spi.*;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.CompletionStage;

public class Engine implements ReactiveStreamsEngine {

    private static final String INVALID_STAGE_MSG = "Invalid stage ";

    private static final ExecutionModel TRANSFORMER;

    static {
        ServiceLoader<ExecutionModel> loader = ServiceLoader.load(ExecutionModel.class);
        Iterator<ExecutionModel> iterator = loader.iterator();
        if (iterator.hasNext()) {
            TRANSFORMER = iterator.next();
        } else {
            TRANSFORMER = i -> i;
        }
    }

    /**
     * Calls the execution model transformer.
     *
     * @param flowable the flowable
     * @param <T>      the type of data
     * @return the decorated flowable if needed
     */
    private static <T> Flowable<T> applyTransformer(Flowable<T> flowable) {
        return TRANSFORMER.transform(flowable);
    }

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
                CompletionStage<R> result = applySubscriber(applyTransformer(flowable), stage, (TerminalOperator) operator);
                return new SubscriberWithCompletionStage<T, R>() {
                    CompletionSubscriberImpl<T, R> subscriber = new CompletionSubscriberImpl<>(processor, result);

                    @Override
                    public CompletionStage<R> getCompletion() {
                        return subscriber.getCompletion();
                    }

                    @Override
                    public Subscriber<T> getSubscriber() {
                        return subscriber;
                    }
                };
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
        if (operator == null) {
            throw new UnsupportedStageException(stage);
        }
        @SuppressWarnings("unchecked") ProcessingStage<I, O> ps = operator.create(this, stage);
        return applyTransformer(ps.process(flowable));
    }

    private <T, R> CompletionStage<R> applySubscriber(Flowable<T> flowable, Stage stage, TerminalOperator operator) {
        if (operator == null) {
            throw new UnsupportedStageException(stage);
        }
        @SuppressWarnings("unchecked") TerminalStage<T, R> ps = operator.create(this, stage);
        return ps.toCompletionStage(applyTransformer(flowable));
    }

    private <O> Flowable<O> createPublisher(Stage stage, PublisherOperator operator) {
        if (operator == null) {
            throw new UnsupportedStageException(stage);
        }
        @SuppressWarnings("unchecked") PublisherStage<O> ps = operator.create(this, stage);
        return applyTransformer(ps.create());
    }
}
