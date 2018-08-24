package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link FlatMapStageFactory}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FlatMapStageFactoryTest extends  StageTestBase {

  private final FlatMapStageFactory factory = new FlatMapStageFactory();

  @Test
  public void create() throws ExecutionException, InterruptedException {
    Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      .subscribeOn(Schedulers.computation());

    List<String> list = ReactiveStreams.fromPublisher(flowable)
      .filter(i -> i < 4)
      .flatMap(this::duplicate)
      .flatMapCompletionStage(this::asString)
      .toList()
      .run(engine).toCompletableFuture().get();

    assertThat(list).containsExactly("1", "1", "2", "2", "3", "3");
  }

  @Test
  public void createFromVertxContext() {
    Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      .subscribeOn(Schedulers.computation());

    executeOnEventLoop(() ->
      ReactiveStreams.fromPublisher(flowable)
        .filter(i -> i < 4)
        .flatMap(this::duplicate)
        .flatMapCompletionStage(this::asString)
        .toList()
        .run(engine)
    ).assertSuccess(Arrays.asList("1", "1", "2", "2", "3", "3"));
  }
  
  private PublisherBuilder<Integer> duplicate(int i) {
    return ReactiveStreams.fromPublisher(Flowable.just(i, i).observeOn(Schedulers.computation()));
  }

  private CompletionStage<String> asString(int i) {
    CompletableFuture<String> cf = new CompletableFuture<>();
    engine.vertx().runOnContext(v -> cf.complete(Objects.toString(i)));
    return cf;
  }

  @Test(expected = NullPointerException.class)
  public void createWithoutStage() {
    factory.create(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void createWithoutFunction() {
    factory.create(null, new Stage.FlatMap(null));
  }

}