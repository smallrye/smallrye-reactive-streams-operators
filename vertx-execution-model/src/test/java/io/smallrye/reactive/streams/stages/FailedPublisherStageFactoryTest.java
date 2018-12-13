package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;


/**
 * Checks the behavior of {@link FailedPublisherStageFactory} when running on the Vert.x Context.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FailedPublisherStageFactoryTest extends StageTestBase {

    @Test
    public void createWithErrorFromVertxContext() {
        Exception failure = new Exception("Boom");
        Callable<CompletionStage<Optional<Integer>>> callable = () -> ReactiveStreams.fromPublisher(Flowable.just(1)
                .observeOn(Schedulers.newThread()))
                .<Integer>flatMap(x -> ReactiveStreams.failed(failure))
                .to(ReactiveStreams.<Integer>builder().findFirst()).run();

        executeOnEventLoop(callable).assertFailure("Boom");
    }
}