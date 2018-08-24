package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;


/**
 * Checks the behavior of {@link FailedPublisherStageFactory}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FailedPublisherStageFactoryTest extends StageTestBase {

  private final FailedPublisherStageFactory factory = new FailedPublisherStageFactory();
  
  @Test
  public void createWithError() {
    Exception failure = new Exception("Boom");
    PublisherStage<Object> boom = factory.create(null, new Stage.Failed(failure));
    TestSubscriber<Object> test = boom.create().test();
    test.assertError(failure);
  }

  @Test
  public void createWithErrorFromVertxContext() {
    Exception failure = new Exception("Boom");
    Callable<CompletionStage<Optional<Integer>>> callable = () -> ReactiveStreams.fromPublisher(Flowable.just(1)
      .observeOn(Schedulers.newThread()))
      .<Integer>flatMap(x -> ReactiveStreams.failed(failure))
      .to(ReactiveStreams.<Integer>builder().findFirst()).run(engine);

    executeOnEventLoop(callable).assertFailure("Boom");
  }

  @Test(expected = NullPointerException.class)
  public void createWithoutError() {
    factory.create(null, new Stage.Failed(null));
  }

  @Test(expected = NullPointerException.class)
  public void createWithoutStage() {
    factory.create(null, null);
  }
}