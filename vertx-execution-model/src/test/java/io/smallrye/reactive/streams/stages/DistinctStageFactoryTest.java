package io.smallrye.reactive.streams.stages;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.Test;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

/**
 * Checks the behavior of the {@link DistinctStageFactory} when running from the Vert.x context.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DistinctStageFactoryTest extends StageTestBase {

    @Test
    public void createOnVertxContext() {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 1, 4, 3, 2, 5, 3, 6, 6, 4, 8, 7, 4, 5, 6, 7, 8, 9, 9, 10)
                .subscribeOn(Schedulers.computation());

        Callable<CompletionStage<List<String>>> callable = () -> ReactiveStreams.fromPublisher(flowable)
                .filter(i -> i < 4)
                .distinct()
                .map(this::asString)
                .toList()
                .run();

        executeOnEventLoop(callable).assertSuccess(Arrays.asList("1", "2", "3"));
    }

    private String asString(int i) {
        return Objects.toString(i);
    }

}
