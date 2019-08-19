package io.smallrye.reactive.streams.stages;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;
import org.junit.Test;

/**
 * Checks the behavior of the {@link Stage.FromCompletionStage} class when running from the Vert.x Context.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FromCompletionStageFactoryTest extends StageTestBase {

    @Test
    public void createFromAlreadyCompletedFutureFromVertxContext() {
        executeOnEventLoop(() -> {
            CompletionStage<String> cs = CompletableFuture.completedFuture("hello");
            return ReactiveStreams.fromCompletionStage(cs).findFirst().run().toCompletableFuture();
        }).assertSuccess(Optional.of("hello"));
    }

    @Test
    public void createFromAlreadyFailedFutureFromVertxContext() {
        executeOnEventLoop(() -> {
            CompletionStage<String> cs = new CompletableFuture<>();
            ((CompletableFuture<String>) cs).completeExceptionally(new Exception("Expected"));
            return ReactiveStreams.fromCompletionStage(cs).findFirst().run();
        }).assertFailure("Expected");
    }

    @Test
    public void createFromFutureGoingToBeCompletedFromVertxContext() {
        CompletableFuture<String> cf = new CompletableFuture<>();

        executeOnEventLoop(() -> {
            CompletionStage<Optional<String>> stage = ReactiveStreams.fromCompletionStage(cf).findFirst().run();
            vertx.setTimer(10, x -> cf.complete("Hello"));
            return stage;
        }).assertSuccess(Optional.of("Hello"));
    }

    @Test
    public void createFromFutureGoingToBeFailedFromVertxContext() {
        CompletableFuture<String> cf = new CompletableFuture<>();

        executeOnEventLoop(() -> {
            CompletionStage<Optional<String>> stage = ReactiveStreams.fromCompletionStage(cf).findFirst().run();
            vertx.setTimer(10, x -> cf.completeExceptionally(new Exception("Expected")));
            return stage;
        }).assertFailure("Expected");
    }

    @Test
    public void createFromFutureGoingToBeCompletedWithNullFromVertxContext() {
        CompletableFuture<Void> cf = new CompletableFuture<>();

        executeOnEventLoop(() -> {
            CompletionStage<Optional<Void>> stage = ReactiveStreams.fromCompletionStage(cf).findFirst().run();
            vertx.setTimer(10, x -> cf.complete(null));
            return stage;
        }).assertFailure("Redeemed value is `null`");
    }
}
