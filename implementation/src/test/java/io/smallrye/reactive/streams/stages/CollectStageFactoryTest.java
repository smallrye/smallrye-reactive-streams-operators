package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.eclipse.microprofile.reactive.streams.tck.spi.QuietRuntimeException;
import org.junit.Test;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

/**
 * Checks the behavior of the {@link CollectStageFactory}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CollectStageFactoryTest extends StageTestBase {

    private final CollectStageFactory factory = new CollectStageFactory();

    @Test
    public void create() throws ExecutionException, InterruptedException {
        TerminalStage<Integer, Integer> terminal = factory.create(null,
                new Stage.Collect(Collectors.summingInt((ToIntFunction<Integer>) value -> value)));

        List<Integer> list = new ArrayList<>();
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .doOnNext(list::add)
                .subscribeOn(Schedulers.computation());
        CompletionStage<Integer> stage = terminal.toCompletionStage(flowable);
        Integer result = stage.toCompletableFuture().get();

        assertThat(result).isEqualTo(1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9 + 10);
        assertThat(list).hasSize(10);
    }

    @Test
    public void createFromVertxContext() {
        Callable<CompletionStage<Integer>> callable = () -> {
            Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .subscribeOn(Schedulers.computation())
                    .map(i -> i + 1);

            return ReactiveStreams.fromPublisher(flowable)
                    .collect(Collectors.summingInt(value -> value)).run(engine);
        };

        executeOnEventLoop(callable).assertSuccess(2 + 3 + 4 + 5 + 6 + 7 + 8 + 9 + 10 + 11);

    }

    @Test(expected = NullPointerException.class)
    public void createWithoutStage() {
        factory.create(engine, null);
    }

    @Test(expected = NullPointerException.class)
    public void createWithoutCollector() {
        factory.create(null, new Stage.Collect(null));
    }

    @Test
    public void collectStageShouldPropagateErrorsFromSupplierThroughCompletionStage() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<Integer> result = infiniteStream()
                .onTerminate(() -> {
                    cancelled.complete(null);
                })
                .collect(Collector.<Integer, Integer, Integer>of(() -> {
                    throw new QuietRuntimeException("failed");
                }, (a, b) -> {
                }, (a, b) -> a + b, Function.identity()))
                .run();
        await().until(cancelled::isDone);
        await().until(() -> result.toCompletableFuture().isDone());
        try {
            result.toCompletableFuture().get();
            fail("Exception expected");
        } catch (Exception e) {
            if (!(e.getCause() instanceof QuietRuntimeException)) {
                fail("Quiet runtime Exception expected");
            }
        }

    }
}
