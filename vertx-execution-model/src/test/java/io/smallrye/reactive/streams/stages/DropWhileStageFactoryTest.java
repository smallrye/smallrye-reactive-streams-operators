package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;

/**
 * Checks the behavior of the {@link DropWhileStageFactory} class when running from the Vert.x context.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DropWhileStageFactoryTest extends StageTestBase {

    @Test
    public void createFromVertxContext() {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        Callable<CompletionStage<List<Integer>>> callable = () ->
                ReactiveStreams.fromPublisher(flowable).dropWhile(i -> i < 6).toList().run();

        executeOnEventLoop(callable).assertSuccess(Arrays.asList(6, 7, 8, 9, 10));
    }

}