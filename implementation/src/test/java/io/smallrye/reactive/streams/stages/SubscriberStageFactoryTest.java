package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.SubscriberBuilder;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link SubscriberStageFactory}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class SubscriberStageFactoryTest extends StageTestBase {

  private final SubscriberStageFactory factory = new SubscriberStageFactory();

  @Test
  public void create() {
    Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      .subscribeOn(Schedulers.computation());

    SubscriberBuilder<Integer, Optional<Integer>> builder = ReactiveStreams.<Integer>builder().findFirst();

    Optional<Integer> optional = ReactiveStreams.fromPublisher(flowable).filter(i -> i > 5)
      .to(builder).run(engine).toCompletableFuture().join();

    assertThat(optional).contains(6);
  }

  @Test
  public void createFromContext() {
    Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      .subscribeOn(Schedulers.computation());
    executeOnEventLoop(() -> {
      SubscriberBuilder<Integer, Optional<Integer>> builder = ReactiveStreams.<Integer>builder().findFirst();
      return ReactiveStreams.fromPublisher(flowable).filter(i -> i > 5)
        .to(builder).run(engine);
    }).assertSuccess(Optional.of(6));
  }

  @Test(expected = NullPointerException.class)
  public void createWithoutStage() {
    factory.create(engine, null);
  }

  @Test(expected = NullPointerException.class)
  public void createWithoutSubscriber() {
    factory.create(engine, new Stage.SubscriberStage(null));
  }


}