package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link OnTerminateStageFactory}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class OnTerminateStageFactoryTest extends StageTestBase {

    @Test
    public void createWithFailureOnVertxContext() {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        AtomicBoolean error = new AtomicBoolean();
        Set<String> threads = new LinkedHashSet<>();
        Callable<CompletionStage<List<String>>> callable = () ->
                ReactiveStreams.fromPublisher(flowable)
                        .filter(i -> i < 4)
                        .map(this::squareOrFailed)
                        .onTerminate(() -> {
                            error.set(true);
                            threads.add(Thread.currentThread().getName());
                        })
                        .map(this::asString)
                        .toList()
                        .run();

        executeOnEventLoop(callable).assertFailure("failed");
        assertThat(error).isTrue();
        assertThat(threads).hasSize(1).containsExactly(getCapturedThreadName());
    }

    @Test
    public void createOnVertxContext() {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        AtomicBoolean completed = new AtomicBoolean();
        Set<String> threads = new LinkedHashSet<>();
        Callable<CompletionStage<List<String>>> callable = () ->
                ReactiveStreams.fromPublisher(flowable)
                        .filter(i -> i < 4)
                        .map(this::square)
                        .onTerminate(() -> {
                            completed.set(true);
                            threads.add(Thread.currentThread().getName());
                        })
                        .map(this::asString)
                        .toList()
                        .run();

        executeOnEventLoop(callable).assertSuccess(Arrays.asList("1", "4", "9"));
        assertThat(completed).isTrue();
        assertThat(threads).hasSize(1).containsExactly(getCapturedThreadName());
    }


    private Integer squareOrFailed(int i) {
        if (i == 2) {
            throw new RuntimeException("failed");
        }
        return i * i;
    }

    private Integer square(int i) {
        return i * i;
    }

    private String asString(int i) {
        return Objects.toString(i);
    }

}