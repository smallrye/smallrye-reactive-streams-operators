package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.function.Predicate;

/**
 * Checks the behavior of the {@link FilterStageFactory} class when running from the Vert.x Context.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FilterStageFactoryTest extends StageTestBase {

    @Test
    public void createFromVertxContext() {
        Predicate<Integer> even = i -> i % 2 == 0;

        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        Callable<CompletionStage<List<Integer>>> callable = () ->
                ReactiveStreams.fromPublisher(flowable).filter(even).toList().run();

        executeOnEventLoop(callable).assertSuccess(Arrays.asList(2, 4, 6, 8, 10));
    }

}