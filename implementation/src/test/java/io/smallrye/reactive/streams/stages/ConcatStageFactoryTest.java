package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.smallrye.reactive.streams.Engine;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.spi.Graph;
import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Checks the behavior of the {@link ConcatStageFactory} class, especially the thread handling.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ConcatStageFactoryTest extends StageTestBase {

    private final ConcatStageFactory factory = new ConcatStageFactory();

    @Test(expected = NullPointerException.class)
    public void testWithoutAStage() {
        factory.create(new Engine(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testWithoutEngine() {
        Graph g1 = () -> Collections.singletonList((Stage.Of) () -> Arrays.asList(1, 2, 3));
        Graph g2 = () -> Collections.singletonList((Stage.Of) () -> Arrays.asList(1, 2, 3));
        factory.create(null, new Stage.Concat() {
            @Override
            public Graph getFirst() {
                return g1;
            }

            @Override
            public Graph getSecond() {
                return g2;
            }
        });
    }

    @Test
    public void testConcatenationWhenAllEmissionAreMadeFromMain() throws ExecutionException, InterruptedException {
        PublisherBuilder<Integer> f1 = ReactiveStreams.of(1, 2, 3);
        PublisherBuilder<Integer> f2 = ReactiveStreams.of(4, 5, 6);

        String currentThreadName = Thread.currentThread().getName();
        LinkedHashSet<String> threads = new LinkedHashSet<>();
        CompletionStage<List<Integer>> list = ReactiveStreams.concat(f1, f2)
                .peek(i -> threads.add(Thread.currentThread().getName()))
                .toList().run();
        await().until(() -> list.toCompletableFuture().isDone());

        List<Integer> ints = list.toCompletableFuture().get();
        assertThat(ints).containsExactly(1, 2, 3, 4, 5, 6);
        assertThat(threads).hasSize(1).contains(currentThreadName);
    }

    @Test
    public void testConcatenationWhenAllEmissionsAreMadeFromDifferentThreads() throws ExecutionException,
            InterruptedException {

        Flowable<Integer> firstStream = Flowable.fromArray(1, 2, 3).observeOn(Schedulers.io());
        Flowable<Integer> secondStream = Flowable.fromArray(4, 5, 6).observeOn(Schedulers.computation());

        LinkedHashSet<String> threads = new LinkedHashSet<>();
        CompletionStage<List<Integer>> list = ReactiveStreams.concat(
                ReactiveStreams.fromPublisher(firstStream),
                ReactiveStreams.fromPublisher(secondStream)
        )
                .peek(i -> threads.add(Thread.currentThread().getName()))
                .toList().run();
        await().until(() -> list.toCompletableFuture().isDone());

        List<Integer> ints = list.toCompletableFuture().get();
        assertThat(ints).containsExactly(1, 2, 3, 4, 5, 6);
        assertThat(threads).hasSize(2);
    }

}