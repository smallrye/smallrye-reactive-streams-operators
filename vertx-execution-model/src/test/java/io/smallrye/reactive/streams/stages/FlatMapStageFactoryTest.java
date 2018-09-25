package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.microprofile.reactive.streams.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.junit.Test;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Checks the behavior of the {@link FlatMapStageFactory} when running from the Vert.x Context.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FlatMapStageFactoryTest extends StageTestBase {

    @Test
    public void createFromVertxContext() {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        executeOnEventLoop(() ->
                ReactiveStreams.fromPublisher(flowable)
                        .filter(i -> i < 4)
                        .flatMap(this::duplicate)
                        .flatMapCompletionStage(this::asString)
                        .toList()
                        .run()
        ).assertSuccess(Arrays.asList("1", "1", "2", "2", "3", "3"));
    }

    private PublisherBuilder<Integer> duplicate(int i) {
        return ReactiveStreams.fromPublisher(Flowable.just(i, i).observeOn(Schedulers.computation()));
    }

    private CompletionStage<String> asString(int i) {
        CompletableFuture<String> cf = new CompletableFuture<>();
        vertx.runOnContext(v -> cf.complete(Objects.toString(i)));
        return cf;
    }
}