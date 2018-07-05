package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link OnErrorStageFactory}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class OnErrorStageFactoryTest extends StageTestBase {

  private final OnErrorStageFactory factory = new OnErrorStageFactory();

  @Test
  public void create() throws ExecutionException, InterruptedException {
    Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      .subscribeOn(Schedulers.computation());

    AtomicReference<Throwable> error = new AtomicReference<>();
    ReactiveStreams.fromPublisher(flowable)
      .filter(i -> i < 4)
      .map(this::squareOrFailed)
      .onError(error::set)
      .map(this::asString)
      .toList()
      .run(engine).toCompletableFuture().exceptionally(x -> Collections.emptyList()).get();
    assertThat(error.get()).hasMessage("failed");
  }

  @Test
  public void createOnVertxContext() {
    Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      .subscribeOn(Schedulers.computation());

    AtomicReference<Throwable> error = new AtomicReference<>();
    Set<String> threads = new LinkedHashSet<>();
    Callable<CompletionStage<List<String>>> callable = () ->
      ReactiveStreams.fromPublisher(flowable)
        .filter(i -> i < 4)
        .map(this::squareOrFailed)
        .onError(err -> {
          error.set(err);
          threads.add(Thread.currentThread().getName());
        })
        .map(this::asString)
        .toList()
        .run(engine);

    executeOnEventLoop(callable).assertFailure("failed");
    assertThat(error.get()).hasMessage("failed");
    assertThat(threads).hasSize(1).containsExactly(getCapturedThreadName());
  }


  private Integer squareOrFailed(int i) {
    if ( i == 2) {
      throw new RuntimeException("failed");
    }
    return i * i;
  }

  private String asString(int i) {
    return Objects.toString(i);
  }

  @Test(expected = NullPointerException.class)
  public void createWithoutStage() {
    factory.create(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void createWithoutFunction() {
    factory.create(null, new Stage.OnError(null));
  }

}