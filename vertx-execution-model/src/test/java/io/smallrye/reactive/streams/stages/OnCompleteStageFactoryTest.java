package io.smallrye.reactive.streams.stages;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.Test;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

/**
 * Checks the behavior of the {@link OnCompleteStageFactory} when running from the Vert.x Context.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class OnCompleteStageFactoryTest extends StageTestBase {

    @Test
    public void createOnVertxContext() {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        AtomicBoolean completed = new AtomicBoolean();
        Set<String> threads = new LinkedHashSet<>();
        Callable<CompletionStage<List<String>>> callable = () -> ReactiveStreams.fromPublisher(flowable)
                .filter(i -> i < 4)
                .map(this::square)
                .onComplete(() -> {
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

    private Integer square(int i) {
        return i * i;
    }

    private String asString(int i) {
        return Objects.toString(i);
    }
}
