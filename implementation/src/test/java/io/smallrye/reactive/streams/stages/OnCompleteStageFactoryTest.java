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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link OnCompleteStageFactory}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class OnCompleteStageFactoryTest extends StageTestBase {

  private final OnCompleteStageFactory factory = new OnCompleteStageFactory();

  @Test
  public void create() throws ExecutionException, InterruptedException {
    Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      .subscribeOn(Schedulers.computation());

    AtomicBoolean completed = new AtomicBoolean();
    ReactiveStreams.fromPublisher(flowable)
      .filter(i -> i < 4)
      .map(this::square)
      .onComplete(() -> completed.set(true))
      .map(this::asString)
      .toList()
      .run(engine).toCompletableFuture().get();
    assertThat(completed).isTrue();
  }

  @Test
  public void createOnVertxContext() {
    Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      .subscribeOn(Schedulers.computation());

    AtomicBoolean completed = new AtomicBoolean();
    Set<String> threads = new LinkedHashSet<>();
    Callable<CompletionStage<List<String>>> callable = () ->
      ReactiveStreams.fromPublisher(flowable)
        .filter(i -> i < 4)
        .map(this::square)
        .onComplete(() -> {
          completed.set(true);
          threads.add(Thread.currentThread().getName());
        })
        .map(this::asString)
        .toList()
        .run(engine);

    executeOnEventLoop(callable).assertSuccess(Arrays.asList("1", "4", "9"));
    assertThat(completed).isTrue();
    assertThat(threads).hasSize(1).containsExactly(getCapturedThreadName());
  }


  private Integer square(int i) {
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
    factory.create(null, new Stage.OnComplete(null));
  }

}