package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;
import org.junit.Test;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link OnErrorStageFactory}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class OnErrorStageFactoryTest extends StageTestBase {

    private final OnErrorStageFactory factory = new OnErrorStageFactory();

    @Test
    public void create() throws ExecutionException, InterruptedException {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        AtomicReference<Throwable> error = new AtomicReference<>();
        ReactiveStreams.fromPublisher(flowable)
                .filter(i -> i < 4)
                .map(this::squareOrFailed)
                .onError(error::set)
                .map(this::asString)
                .toList()
                .run().toCompletableFuture().exceptionally(x -> Collections.emptyList()).get();
        assertThat(error.get()).hasMessage("Uni.from().failure");
    }

    private Integer squareOrFailed(int i) {
        if (i == 2) {
            throw new RuntimeException("Uni.from().failure");
        }
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