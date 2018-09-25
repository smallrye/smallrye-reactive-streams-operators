package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.junit.Test;

import java.util.Optional;

/**
 * Checks the behavior of the {@link FindFirstStageFactory} when running from the Vert.x Context.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FindFirstStageFactoryTest extends StageTestBase {

    @Test
    public void createFromContext() {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());
        executeOnEventLoop(() ->
                ReactiveStreams.fromPublisher(flowable).filter(i -> i > 5)
                        .findFirst().run()).assertSuccess(Optional.of(6));
    }

}