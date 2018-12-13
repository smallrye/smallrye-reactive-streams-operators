package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Checks the behavior of the {@link CollectStageFactory} when running from a Vert.x context
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CollectStageFactoryTest extends StageTestBase {

    @Test
    public void createFromVertxContext() {
        Callable<CompletionStage<Integer>> callable = () -> {
            Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .subscribeOn(Schedulers.computation())
                    .map(i -> i + 1);

            return ReactiveStreams.fromPublisher(flowable)
                    .collect(Collectors.summingInt(value -> value)).run();
        };

        executeOnEventLoop(callable).assertSuccess(2 + 3 + 4 + 5 + 6 + 7 + 8 + 9 + 10 + 11);

    }
}
