package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link OnErrorStageFactory} when running from the Vert.x Context.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class OnErrorStageFactoryTest extends StageTestBase {

    @Test
    public void createOnVertxContext() {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        AtomicReference<Throwable> error = new AtomicReference<>();
        Set<String> threads = new LinkedHashSet<>();
        Callable<CompletionStage<List<String>>> callable = () ->
                ReactiveStreams.fromPublisher(flowable)
                        .filter(i -> i < 4)
                        .map(this::squareOrFailed)
                        .onError(err -> {
                            error.set(err);
                            threads.add(Thread.currentThread().getName());
                        })
                        .map(this::asString)
                        .toList()
                        .run();

        executeOnEventLoop(callable).assertFailure("failed");
        assertThat(error.get()).hasMessage("failed");
        assertThat(threads).hasSize(1).containsExactly(getCapturedThreadName());
    }


    private Integer squareOrFailed(int i) {
        if (i == 2) {
            throw new RuntimeException("failed");
        }
        return i * i;
    }

    private String asString(int i) {
        return Objects.toString(i);
    }

}