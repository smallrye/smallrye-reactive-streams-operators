package io.smallrye.reactive.streams.stages;

import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link FromIterableStageFactory} class.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FromIterableStageFactoryTest extends StageTestBase {


  private final FromIterableStageFactory factory = new FromIterableStageFactory();

  @Test
  public void create() throws ExecutionException, InterruptedException {
    List<Integer> list = ReactiveStreams.of(1, 2, 3).toList().run().toCompletableFuture().get();
    assertThat(list).containsExactly(1, 2, 3);

    Optional<Integer> res = ReactiveStreams.of(25).findFirst().run().toCompletableFuture().get();
    assertThat(res).contains(25);

    Optional<?> empty = ReactiveStreams.fromIterable(Collections.emptyList()).findFirst().run().toCompletableFuture().get();
    assertThat(empty).isEmpty();
  }

  @Test
  public void createFromVertxContext() {

    executeOnEventLoop(() -> ReactiveStreams.of(1, 2, 3).toList().run(engine)).assertSuccess(Arrays.asList(1, 2, 3));

    executeOnEventLoop(() -> ReactiveStreams.of(25).findFirst().run(engine)).assertSuccess(Optional.of(25));

    executeOnEventLoop(() -> ReactiveStreams.fromIterable(Collections.emptyList()).findFirst().run(engine)).assertSuccess(Optional.empty());
  }


  @Test(expected = NullPointerException.class)
  public void createWithoutStage() {
    factory.create(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void createWithoutFunction() {
    factory.create(null, new Stage.Of(null));
  }

}