package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link DropWhileStageFactory} class.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DropWhileStageFactoryTest extends StageTestBase {

  private final DropWhileStageFactory factory = new DropWhileStageFactory();

  @Test
  public void create() throws ExecutionException, InterruptedException {
    Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      .subscribeOn(Schedulers.computation());
    List<Integer> list = ReactiveStreams.fromPublisher(flowable).dropWhile(i -> i < 6).toList().run(engine)
      .toCompletableFuture().get();
    assertThat(list).hasSize(5).containsExactly(6, 7, 8, 9, 10);
  }

  @Test
  public void createFromVertxContext() {
    Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      .subscribeOn(Schedulers.computation());

    Callable<CompletionStage<List<Integer>>> callable = () ->
      ReactiveStreams.fromPublisher(flowable).dropWhile(i -> i < 6).toList().run(engine);

    executeOnEventLoop(callable).assertSuccess(Arrays.asList(6, 7, 8, 9, 10));
  }

  @Test(expected = NullPointerException.class)
  public void createWithoutStage() {
    factory.create(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void createWithoutPredicate() {
    factory.create(null, new Stage.DropWhile(null));
  }


}