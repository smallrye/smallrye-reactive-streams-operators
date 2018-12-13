package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.operators.ProcessorBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.Test;
import org.reactivestreams.Processor;

import java.util.Arrays;

/**
 * Checks the behavior of the {@link ProcessorStageFactory} when running from the Vert.x Context.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ProcessorStageFactoryTest extends StageTestBase {

    @Test
    public void createWithProcessorsFromVertxContext() {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        executeOnEventLoop(() ->
                ReactiveStreams.fromPublisher(flowable)
                        .filter(i -> i < 4)
                        .via(duplicateProcessor())
                        .via(asStringProcessor())
                        .toList()
                        .run()
        ).assertSuccess(Arrays.asList("1", "1", "2", "2", "3", "3"));
    }

    @Test
    public void createWithProcessorBuildersFromVertxContext() {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        executeOnEventLoop(() ->
                ReactiveStreams.fromPublisher(flowable)
                        .filter(i -> i < 4)
                        .via(duplicateProcessorBuilder())
                        .via(asStringProcessorBuilder())
                        .toList()
                        .run()
        ).assertSuccess(Arrays.asList("1", "1", "2", "2", "3", "3"));
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

}