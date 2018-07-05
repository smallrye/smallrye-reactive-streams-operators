package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.spi.Graph;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Checks the behavior of the {@link ConcatStageFactory} class, especially the thread handling.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ConcatStageFactoryTest extends StageTestBase {

  private final ConcatStageFactory factory = new ConcatStageFactory();
  
  @Test(expected = NullPointerException.class)
  public void testWithoutAStage() {
    factory.create(engine, null);
  }

  @Test(expected = NullPointerException.class)
  public void testWithoutEngine() {
    Graph g1 = new Graph(Collections.singletonList(new Stage.Of(Arrays.asList(1, 2, 3))));
    Graph g2 = new Graph(Collections.singletonList(new Stage.Of(Arrays.asList(1, 2, 3))));
    factory.create(null, new Stage.Concat(g1, g2));
  }

  @Test
  public void testConcatenationWhenAllEmissionAreMadeFromMain() throws ExecutionException, InterruptedException {
    Graph g1 = new Graph(Collections.singletonList(new Stage.Of(Arrays.asList(1, 2, 3))));
    Graph g2 = new Graph(Collections.singletonList(new Stage.Of(Arrays.asList(4, 5, 6))));
    String currentThreadName = Thread.currentThread().getName();
    LinkedHashSet<String> threads = new LinkedHashSet<>();
    PublisherStage<Integer> stage = factory.create(engine, new Stage.Concat(g1, g2));
    CompletionStage<List<Integer>> list = ReactiveStreams.fromPublisher(stage.create())
      .peek(i -> threads.add(Thread.currentThread().getName()))
      .toList().run(engine);
    await().until(() -> list.toCompletableFuture().isDone());

    List<Integer> ints = list.toCompletableFuture().get();
    assertThat(ints).containsExactly(1, 2, 3, 4, 5, 6);
    assertThat(threads).hasSize(1).contains(currentThreadName);
  }

  @Test
  public void testConcatenationWhenAllEmissionsAreMadeFromDifferentThreads() throws ExecutionException,
    InterruptedException {
    Flowable<Integer> firstStream = Flowable.fromArray(1, 2, 3).observeOn(Schedulers.io());
    Flowable<Integer> secondStream = Flowable.fromArray(4, 5, 6).observeOn(Schedulers.computation());
    Graph g1 = new Graph(Collections.singletonList(new Stage.PublisherStage(firstStream)));
    Graph g2 = new Graph(Collections.singletonList(new Stage.PublisherStage(secondStream)));
    LinkedHashSet<String> threads = new LinkedHashSet<>();
    PublisherStage<Integer> stage = factory.create(engine, new Stage.Concat(g1, g2));
    CompletionStage<List<Integer>> list = ReactiveStreams.fromPublisher(stage.create())
      .peek(i -> threads.add(Thread.currentThread().getName()))
      .toList().run(engine);
    await().until(() -> list.toCompletableFuture().isDone());

    List<Integer> ints = list.toCompletableFuture().get();
    assertThat(ints).containsExactly(1, 2, 3, 4, 5, 6);
    assertThat(threads).hasSize(2);
  }

  @Test
  public void testConcatenationWhenAllEmissionsAreMadeFromDifferentThreadsButCreationIsOnVertxContext() {
    Flowable<Integer> firstStream = Flowable.fromArray(1, 2, 3).observeOn(Schedulers.io());
    Flowable<Integer> secondStream = Flowable.fromArray(4, 5, 6).observeOn(Schedulers.computation());
    Graph g1 = new Graph(Collections.singletonList(new Stage.PublisherStage(firstStream)));
    Graph g2 = new Graph(Collections.singletonList(new Stage.PublisherStage(secondStream)));
    LinkedHashSet<String> threads = new LinkedHashSet<>();
    Callable<CompletionStage<List<Integer>>> callable = () -> {
      PublisherStage<Integer> stage = factory.create(engine, new Stage.Concat(g1, g2));
      return ReactiveStreams.fromPublisher(stage.create())
        .peek(i -> threads.add(Thread.currentThread().getName()))
        .toList().run(engine);
    };

    executeOnEventLoop(callable).assertSuccess(Arrays.asList(1, 2, 3, 4, 5, 6));
    assertThat(threads).hasSize(1).contains(getCapturedThreadName());
  }

}