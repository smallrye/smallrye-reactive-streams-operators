package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link FindFirstStageFactory}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FindFirstStageFactoryTest extends StageTestBase {

    private final FindFirstStageFactory factory = new FindFirstStageFactory();

    @Test
    public void create() {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        Optional<Integer> optional = ReactiveStreams.fromPublisher(flowable).filter(i -> i > 5)
                .findFirst().run().toCompletableFuture().join();

        assertThat(optional).contains(6);
    }

    @Test(expected = NullPointerException.class)
    public void createWithoutStage() {
        factory.create(null, null);
    }


}