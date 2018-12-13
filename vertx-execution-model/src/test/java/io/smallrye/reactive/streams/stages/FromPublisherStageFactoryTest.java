package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

/**
 * Checks the behavior of the {@link FromPublisherStageFactory} class when running from the Vert.x Context.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FromPublisherStageFactoryTest extends StageTestBase {

    @Test
    public void createFromVertxContext() {
        executeOnEventLoop(() -> ReactiveStreams.fromPublisher(Flowable.fromArray(1, 2, 3)).toList().run()).assertSuccess(Arrays.asList(1, 2, 3));

        executeOnEventLoop(() -> ReactiveStreams.fromPublisher(Flowable.just(25)).findFirst().run()).assertSuccess(Optional.of(25));

        executeOnEventLoop(() -> ReactiveStreams.fromPublisher(Flowable.empty()).findFirst().run()).assertSuccess(Optional.empty());
    }

}