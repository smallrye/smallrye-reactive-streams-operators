package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.ProcessorBuilder;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.junit.Test;
import org.reactivestreams.Processor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link ProcessorStageFactory}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ProcessorStageFactoryTest extends StageTestBase {

    private final ProcessorStageFactory factory = new ProcessorStageFactory();

    @Test
    public void createWithProcessors() throws ExecutionException, InterruptedException {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        List<String> list = ReactiveStreams.fromPublisher(flowable)
                .filter(i -> i < 4)
                .via(duplicateProcessor())
                .via(asStringProcessor())
                .toList()
                .run().toCompletableFuture().get();

        assertThat(list).containsExactly("1", "1", "2", "2", "3", "3");
    }

    @Test
    public void createWithProcessorBuilders() throws ExecutionException, InterruptedException {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        List<String> list = ReactiveStreams.fromPublisher(flowable)
                .filter(i -> i < 4)
                .via(duplicateProcessorBuilder())
                .via(asStringProcessorBuilder())
                .toList()
                .run().toCompletableFuture().get();

        assertThat(list).containsExactly("1", "1", "2", "2", "3", "3");
    }

    private ProcessorBuilder<Integer, Integer> duplicateProcessorBuilder() {
        return ReactiveStreams.<Integer>builder().flatMapIterable(i -> Arrays.asList(i, i));
    }

    private Processor<Integer, Integer> duplicateProcessor() {
        return duplicateProcessorBuilder().buildRs();
    }

    private ProcessorBuilder<Integer, String> asStringProcessorBuilder() {
        return ReactiveStreams.<Integer>builder().map(Object::toString);
    }

    private Processor<Integer, String> asStringProcessor() {
        return asStringProcessorBuilder().buildRs();
    }

    @Test(expected = NullPointerException.class)
    public void createWithoutStage() {
        factory.create(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void createWithoutFunction() {
        factory.create(null, new Stage.ProcessorStage(null));
    }

}