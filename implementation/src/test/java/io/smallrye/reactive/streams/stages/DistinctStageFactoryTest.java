package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link DistinctStageFactory}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DistinctStageFactoryTest extends StageTestBase {

  private final DistinctStageFactory factory = new DistinctStageFactory();

  @Test
  public void create() throws ExecutionException, InterruptedException {
    Flowable<Integer> flowable = Flowable.fromArray(1, 2, 1, 4, 3, 2, 5, 3, 6, 6, 4, 8, 7, 4, 5, 6, 7, 8, 9, 9, 10)
      .subscribeOn(Schedulers.computation());

    List<String> list = ReactiveStreams.fromPublisher(flowable)
      .filter(i -> i < 4)
      .distinct()
      .map(this::asString)
      .toList()
      .run(engine).toCompletableFuture().get();

    assertThat(list).containsExactly("1", "2", "3");
  }

  @Test
  public void createOnVertxContext() {
    Flowable<Integer> flowable = Flowable.fromArray(1, 2, 1, 4, 3, 2, 5, 3, 6, 6, 4, 8, 7, 4, 5, 6, 7, 8, 9, 9, 10)
      .subscribeOn(Schedulers.computation());

    Callable<CompletionStage<List<String>>> callable = () ->
      ReactiveStreams.fromPublisher(flowable)
        .filter(i -> i < 4)
        .distinct()
        .map(this::asString)
        .toList()
        .run(engine);

    executeOnEventLoop(callable).assertSuccess(Arrays.asList("1", "2", "3"));
  }


  private String asString(int i) {
    return Objects.toString(i);
  }

  @Test(expected = NullPointerException.class)
  public void createWithoutStage() {
    factory.create(null, null);
  }
  
}