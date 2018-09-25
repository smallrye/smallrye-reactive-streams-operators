package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link ConcatStageFactory} class, especially the thread handling when running from a Vert.x context.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ConcatStageFactoryTest extends StageTestBase {

    @Test
    public void testConcatenationWhenAllEmissionsAreMadeFromDifferentThreadsButCreationIsOnVertxContext() {
        Flowable<Integer> firstStream = Flowable.fromArray(1, 2, 3).observeOn(Schedulers.io());
        Flowable<Integer> secondStream = Flowable.fromArray(4, 5, 6).observeOn(Schedulers.computation());


        LinkedHashSet<String> threads = new LinkedHashSet<>();
        Callable<CompletionStage<List<Integer>>> callable = () ->
                ReactiveStreams
                        .concat(ReactiveStreams.fromPublisher(firstStream), ReactiveStreams.fromPublisher(secondStream))
                        .peek(i -> threads.add(Thread.currentThread().getName()))
                        .toList().run();

        executeOnEventLoop(callable).assertSuccess(Arrays.asList(1, 2, 3, 4, 5, 6));
        assertThat(threads).hasSize(1).contains(getCapturedThreadName());
    }

}