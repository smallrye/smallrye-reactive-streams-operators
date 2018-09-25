package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.junit.After;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link FlatMapIterableStageFactory}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FlatMapIterableStageFactoryTest extends StageTestBase {

    private final FlatMapIterableStageFactory factory = new FlatMapIterableStageFactory();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @After
    public void cleanup() {
        executor.shutdown();
    }

    @Test
    public void create() throws ExecutionException, InterruptedException {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        List<String> list = ReactiveStreams.fromPublisher(flowable)
                .filter(i -> i < 4)
                .flatMapIterable(this::duplicate)
                .flatMapCompletionStage(this::asString)
                .toList()
                .run().toCompletableFuture().get();

        assertThat(list).containsExactly("1", "1", "2", "2", "3", "3");
    }

    private List<Integer> duplicate(int i) {
        return Arrays.asList(i, i);
    }

    private CompletionStage<String> asString(int i) {
        CompletableFuture<String> cf = new CompletableFuture<>();
        executor.submit(() -> cf.complete(Objects.toString(i)));
        return cf;
    }

    @Test(expected = NullPointerException.class)
    public void createWithoutStage() {
        factory.create(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void createWithoutFunction() {
        factory.create(null, new Stage.FlatMapIterable(null));
    }

}