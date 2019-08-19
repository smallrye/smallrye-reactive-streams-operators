package io.smallrye.reactive.streams.stages;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.Test;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

/**
 * Checks the behavior of the {@link FlatMapIterableStageFactory} when running from the Vert.x Context.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FlatMapIterableStageFactoryTest extends StageTestBase {

    @Test
    public void createFromVertxContext() {
        Flowable<Integer> flowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .subscribeOn(Schedulers.computation());

        executeOnEventLoop(() -> ReactiveStreams.fromPublisher(flowable)
                .filter(i -> i < 4)
                .flatMapIterable(this::duplicate)
                .flatMapCompletionStage(this::asString)
                .toList()
                .run()).assertSuccess(Arrays.asList("1", "1", "2", "2", "3", "3"));
    }

    private List<Integer> duplicate(int i) {
        return Arrays.asList(i, i);
    }

    private CompletionStage<String> asString(int i) {
        CompletableFuture<String> cf = new CompletableFuture<>();
        vertx.runOnContext(v -> cf.complete(Objects.toString(i)));
        return cf;
    }

}
