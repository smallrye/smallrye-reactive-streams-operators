package io.smallrye.reactive.streams.stages;

import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

/**
 * Checks the behavior of the {@link org.eclipse.microprofile.reactive.streams.spi.Stage.FromCompletionStage} class.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FromCompletionStageFactoryTest extends StageTestBase {


    private final FromCompletionStageFactory factory = new FromCompletionStageFactory();

    @Test
    public void createFromAlreadyCompletedFuture() {
        CompletionStage<String> cs = CompletableFuture.completedFuture("hello");
        List<String> list = ReactiveStreams.fromCompletionStage(cs).toList().run().toCompletableFuture().join();
        assertThat(list).containsExactly("hello");
    }

    @Test
    public void createFromAlreadyCompletedFutureFromVertxContext() {
        executeOnEventLoop(() -> {
            CompletionStage<String> cs = CompletableFuture.completedFuture("hello");
            return ReactiveStreams.fromCompletionStage(cs).findFirst().run().toCompletableFuture();
        }).assertSuccess(Optional.of("hello"));
    }

    @Test
    public void createFromAlreadyFailedFuture() {
        CompletionStage<String> cs = new CompletableFuture<>();
        ((CompletableFuture<String>) cs).completeExceptionally(new Exception("Expected"));

        try {
            ReactiveStreams.fromCompletionStage(cs).findFirst().run().toCompletableFuture().join();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("Expected");
        }
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
    public void createFromFutureGoingToBeCompleted() {
        CompletableFuture<String> cf = new CompletableFuture<>();
        CompletionStage<Optional<String>> stage = ReactiveStreams.fromCompletionStage(cf).findFirst().run();

        AtomicBoolean done = new AtomicBoolean();
        stage.whenComplete((res, err) -> {
            assertThat(err).isNull();
            assertThat(res).contains("Hello");
            done.set(true);
        });

        new Thread(() -> cf.complete("Hello")).start();
        await().untilAtomic(done, is(true));
    }

    @Test
    public void createFromFutureGoingToBeCompletedFromVertxContext() {
        CompletableFuture<String> cf = new CompletableFuture<>();

        executeOnEventLoop(() -> {
            CompletionStage<Optional<String>> stage = ReactiveStreams.fromCompletionStage(cf).findFirst().run(engine);
            engine.vertx().setTimer(10, x -> cf.complete("Hello"));
            return stage;
        }).assertSuccess(Optional.of("Hello"));
    }

    @Test
    public void createFromFutureGoingToBeFailed() {
        CompletableFuture<String> cf = new CompletableFuture<>();
        CompletionStage<Optional<String>> stage = ReactiveStreams.fromCompletionStage(cf).findFirst().run();

        AtomicBoolean done = new AtomicBoolean();
        stage.whenComplete((res, err) -> {
            assertThat(err).isNotNull().hasMessageContaining("Expected");
            assertThat(res).isNull();
            done.set(true);
        });

        new Thread(() -> cf.completeExceptionally(new Exception("Expected"))).start();
        await().untilAtomic(done, is(true));
    }

    @Test
    public void createFromFutureGoingToBeFailedFromVertxContext() {
        CompletableFuture<String> cf = new CompletableFuture<>();

        executeOnEventLoop(() -> {
            CompletionStage<Optional<String>> stage = ReactiveStreams.fromCompletionStage(cf).findFirst().run();
            engine.vertx().setTimer(10, x -> cf.completeExceptionally(new Exception("Expected")));
            return stage;
        }).assertFailure("Expected");
    }

    @Test
    public void createFromFutureCompletedWithNull() {
        CompletableFuture<Void> cf = new CompletableFuture<>();
        CompletionStage<Optional<Void>> stage = ReactiveStreams.fromCompletionStage(cf).findFirst().run();

        AtomicBoolean done = new AtomicBoolean();
        stage.whenComplete((res, err) -> {
            assertThat(err).isNotNull().isInstanceOf(NullPointerException.class);
            assertThat(res).isNull();
            done.set(true);
        });

        new Thread(() -> cf.complete(null)).start();
        await().untilAtomic(done, is(true));
    }

    @Test
    public void createFromFutureGoingToBeCompletedWithNullFromVertxContext() {
        CompletableFuture<Void> cf = new CompletableFuture<>();

        executeOnEventLoop(() -> {
            CompletionStage<Optional<Void>> stage = ReactiveStreams.fromCompletionStage(cf).findFirst().run();
            engine.vertx().setTimer(10, x -> cf.complete(null));
            return stage;
        }).assertFailure("Redeemed value is `null`");
    }


    @Test(expected = NullPointerException.class)
    public void createWithoutStage() {
        factory.create(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void createWithoutFunction() {
        factory.create(null, new Stage.FromCompletionStage(null));
    }

}
