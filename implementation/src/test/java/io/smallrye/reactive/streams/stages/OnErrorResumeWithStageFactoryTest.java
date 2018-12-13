package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
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
                .run().toCompletableFuture().exceptionally(x -> Collections.emptyList()).get();

        assertThat(list).hasSize(10);
    }

    @Test
    public void createAndFailAgain() throws ExecutionException, InterruptedException {
        AtomicReference<Throwable> error = new AtomicReference<>();
        List<Integer> list = ReactiveStreams.<Integer>failed(new RuntimeException("BOOM"))
                .onErrorResumeWith(t -> ReactiveStreams.failed(new RuntimeException("Failed")))
                .toList()
                .run().toCompletableFuture().exceptionally(x -> {
                    error.set(x);
                    return Collections.emptyList();
                }).get();

        assertThat(list).hasSize(0);
        assertThat(error.get()).hasMessage("Failed");
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