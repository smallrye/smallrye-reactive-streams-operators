package io.smallrye.reactive.streams;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.stages.*;
import io.vertx.reactivex.core.Context;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import org.eclipse.microprofile.reactive.streams.CompletionSubscriber;
import org.eclipse.microprofile.reactive.streams.spi.Graph;
import org.eclipse.microprofile.reactive.streams.spi.ReactiveStreamsEngine;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.eclipse.microprofile.reactive.streams.spi.UnsupportedStageException;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import io.smallrye.reactive.streams.utils.ConnectableProcessor;
import io.smallrye.reactive.streams.utils.WrappedProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class Engine implements ReactiveStreamsEngine {

  private static final Map<Class, ProcessingStageFactory> PROCESSOR_STAGES = new HashMap<>();
  private static Map<Class, PublisherStageFactory> PUBLISHER_STAGES = new HashMap<>();
  private static Map<Class, TerminalStageFactory> SUBSCRIBER_STAGES = new HashMap<>();

  static {
    PROCESSOR_STAGES.put(Stage.Filter.class, new FilterStageFactory());
    PROCESSOR_STAGES.put(Stage.FlatMap.class, new FlatMapStageFactory());
    PROCESSOR_STAGES.put(Stage.FlatMapCompletionStage.class, new FlatMapCompletionStageFactory());
    PROCESSOR_STAGES.put(Stage.FlatMapIterable.class, new FlatMapIterableStageFactory());
    PROCESSOR_STAGES.put(Stage.Map.class, new MapStageFactory());
    PROCESSOR_STAGES.put(Stage.Peek.class, new PeekStageFactory());
    PROCESSOR_STAGES.put(Stage.ProcessorStage.class, new ProcessorStageFactory());
    PROCESSOR_STAGES.put(Stage.TakeWhile.class, new TakeWhileStageFactory());

    PUBLISHER_STAGES.put(Stage.Concat.class, new ConcatStageFactory());
    PUBLISHER_STAGES.put(Stage.Failed.class, new FailedPublisherStageFactory());
    PUBLISHER_STAGES.put(Stage.Of.class, new FromIterableStageFactory());
    PUBLISHER_STAGES.put(Stage.PublisherStage.class, new FromPublisherStageFactory());

    SUBSCRIBER_STAGES.put(Stage.Cancel.class, new CancelStageFactory());
    SUBSCRIBER_STAGES.put(Stage.Collect.class, new CollectStageFactory());
    SUBSCRIBER_STAGES.put(Stage.FindFirst.class, new FindFirstStageFactory());
    SUBSCRIBER_STAGES.put(Stage.SubscriberStage.class, new SubscriberStageFactory());
  }

  //TODO Should the vert.x instance be shared among several instance of engine?
  // It may be a little bit complicated when we will implement async()

  private final Vertx vertx;

  public Engine() {
    this(Vertx.vertx());
  }

  public Engine(Vertx vertx) {
    this.vertx = vertx;
  }

  public Engine(io.vertx.core.Vertx vertx) {
    this(new Vertx(vertx));
  }

  public void close() {
    vertx.close();
  }

  @Override
  public <T> Publisher<T> buildPublisher(Graph graph) throws UnsupportedStageException {
    Flowable<T> flowable = null;
    for (Stage stage : graph.getStages()) {
      if (flowable == null) {
        flowable = createPublisher(stage);
      } else {
        flowable = applyProcessors(flowable, stage);
      }
    }
    return flowable;
  }

  /**
   * If the caller thread is a Vert.x Context, switch back to the context when the pipeline
   * completes.
   *
   * @param flowable the flowable
   * @param <T>      the type of data
   * @return the decorated flowable if needed
   */
  private <T> Flowable<T> injectThreadSwitchIfNeeded(Flowable<T> flowable) {
    Context context = Vertx.currentContext();
    if (context != null) {
      return flowable.compose((f) -> f.observeOn(RxHelper.scheduler(context)));
    }
    return flowable;
  }

  @Override
  public <T, R> CompletionSubscriber<T, R> buildSubscriber(Graph graph)
    throws UnsupportedStageException {
    Processor<T, T> processor = new ConnectableProcessor<>();

    Flowable<T> flowable = Flowable.fromPublisher(processor);
    for (Stage stage : graph.getStages()) {
      if (stage.hasOutlet()) {
        flowable = applyProcessors(flowable, stage);
      } else {
        CompletionStage<R> result = applySubscriber(injectThreadSwitchIfNeeded(flowable), stage);
        return CompletionSubscriber.of(processor, result);
      }
    }

    throw new IllegalArgumentException("The graph does not have a valid final stage");
  }

  @Override
  public <T, R> Processor<T, R> buildProcessor(Graph graph) throws UnsupportedStageException {
    Processor<T, T> processor = new ConnectableProcessor<>();

    Flowable<T> flowable = Flowable.fromPublisher(processor);
    for (Stage stage : graph.getStages()) {
      flowable = applyProcessors(flowable, stage);
    }

    //noinspection unchecked
    return (Processor<T, R>) new WrappedProcessor<>(processor, flowable);
  }

  @Override
  public <T> CompletionStage<T> buildCompletion(Graph graph) throws UnsupportedStageException {
    Flowable<?> flowable = null;
    for (Stage stage : graph.getStages()) {
      if (flowable == null) {
        flowable = createPublisher(stage);
      } else if (stage.hasOutlet()) {
        flowable = applyProcessors(flowable, stage);
      } else {
        return applySubscriber(flowable, stage);
      }
    }

    throw new IllegalArgumentException("Graph did not have terminal stage");
  }

  private <I, O> Flowable<O> applyProcessors(Flowable<I> flowable, Stage stage) {
    if (!stage.hasOutlet() && !stage.hasInlet()) {
      throw new IllegalArgumentException("Invalid stage " + stage
        + " - expected one inlet and one outlet.");
    }
    ProcessingStageFactory factory = PROCESSOR_STAGES.get(stage.getClass());
    if (factory == null) {
      throw new UnsupportedStageException(stage);
    }
    @SuppressWarnings("unchecked") ProcessingStage<I, O> ps = factory.create(this, stage);
    return injectThreadSwitchIfNeeded(ps.process(flowable));
  }

  private <T, R> CompletionStage<R> applySubscriber(Flowable<T> flowable, Stage stage) {
    if (stage.hasOutlet() || !stage.hasInlet()) {
      throw new IllegalArgumentException("Invalid stage " + stage
        + " - expected one inlet and no outlet.");
    }
    TerminalStageFactory factory = SUBSCRIBER_STAGES.get(stage.getClass());
    if (factory == null) {
      throw new UnsupportedStageException(stage);
    }
    @SuppressWarnings("unchecked") TerminalStage<T, R> ps = factory.create(this, stage);
    return ps.toCompletionStage(injectThreadSwitchIfNeeded(flowable));
  }

  private <O> Flowable<O> createPublisher(Stage stage) {
    if (!stage.hasOutlet() || stage.hasInlet()) {
      throw new IllegalArgumentException("Invalid stage " + stage
        + " - expected no inlet and one outlet.");
    }
    PublisherStageFactory factory = PUBLISHER_STAGES.get(stage.getClass());
    if (factory == null) {
      throw new UnsupportedStageException(stage);
    }
    @SuppressWarnings("unchecked") PublisherStage<O> ps = factory.create(this, stage);
    return injectThreadSwitchIfNeeded(ps.create());
  }

  public Vertx vertx() {
    return vertx;
  }
}
