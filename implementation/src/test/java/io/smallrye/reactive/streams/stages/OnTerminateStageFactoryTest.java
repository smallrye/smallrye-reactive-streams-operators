package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.Test;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link OnTerminateStageFactory}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class OnTerminateStageFactoryTest extends StageTestBase {

    private final OnTerminateStageFactory factory = new OnTerminateStageFactory();

    @Test
    public void createWithFailure() throws ExecutionException, InterruptedException {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        AtomicBoolean error = new AtomicBoolean();
        ReactiveStreams.fromPublisher(flowable)
                .filter(i -> i < 4)
                .map(this::squareOrFailed)
                .onTerminate(() -> error.set(true))
                .map(this::asString)
                .toList()
                .run().toCompletableFuture().exceptionally(x -> Collections.emptyList()).get();
        assertThat(error).isTrue();
    }

    @Test
    public void create() throws ExecutionException, InterruptedException {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        AtomicBoolean completed = new AtomicBoolean();
        ReactiveStreams.fromPublisher(flowable)
                .filter(i -> i < 4)
                .map(this::square)
                .onTerminate(() -> completed.set(true))
                .map(this::asString)
                .toList()
                .run().toCompletableFuture().get();
        assertThat(completed).isTrue();
    }

    private Integer squareOrFailed(int i) {
        if (i == 2) {
            throw new RuntimeException("Uni.from().failure");
        }
        return i * i;
    }

    private Integer square(int i) {
        return i * i;
    }

    private String asString(int i) {
        return Objects.toString(i);
    }

    @Test(expected = NullPointerException.class)
    public void createWithoutStage() {
        factory.create(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void createWithoutFunction() {
        factory.create(null, () -> null);
    }

}