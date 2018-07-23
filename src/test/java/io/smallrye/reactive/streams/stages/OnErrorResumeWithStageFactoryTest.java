package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link OnErrorResumeStageFactory}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class OnErrorResumeWithStageFactoryTest extends StageTestBase {

    private final OnErrorResumeStageFactory factory = new OnErrorResumeStageFactory();

    @Test
    public void create() throws ExecutionException, InterruptedException {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        List<Integer> list = ReactiveStreams.<Integer>failed(new Exception("BOOM"))
                .onErrorResumeWith(t -> ReactiveStreams.fromPublisher(flowable))
                .toList()
                .run(engine).toCompletableFuture().exceptionally(x -> Collections.emptyList()).get();

        assertThat(list).hasSize(10);
    }

    @Test
    public void createAndFailAgain() throws ExecutionException, InterruptedException {
        AtomicReference<Throwable> error = new AtomicReference<>();
        List<Integer> list = ReactiveStreams.<Integer>failed(new RuntimeException("BOOM"))
                .onErrorResumeWith(t -> ReactiveStreams.failed(new RuntimeException("Failed")))
                .toList()
                .run(engine).toCompletableFuture().exceptionally(x -> {
                    error.set(x);
                    return Collections.emptyList();
                }).get();

        assertThat(list).hasSize(0);
        assertThat(error.get()).hasMessage("Failed");

    }

    @Test
    public void createOnVertxContext() {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        Set<String> threads = new LinkedHashSet<>();
        Callable<CompletionStage<List<Integer>>> callable = () ->
                ReactiveStreams.<Integer>failed(new Exception("BOOM"))
                        .onErrorResumeWithPublisher(t -> flowable.observeOn(Schedulers.computation()))
                        .peek(x -> threads.add(Thread.currentThread().getName()))
                        .toList()
                        .run(engine);

        executeOnEventLoop(callable).assertSuccess(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        assertThat(threads).hasSize(1).containsExactly(getCapturedThreadName());
    }

    @Test(expected = NullPointerException.class)
    public void createWithoutStage() {
        factory.create(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void createWithoutFunction() {
        factory.create(null, new Stage.OnErrorResume(null));
    }

}