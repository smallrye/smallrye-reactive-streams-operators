package io.smallrye.reactive.streams.stages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.spi.Stage;
import org.junit.Test;
import org.reactivestreams.Subscription;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.smallrye.reactive.streams.operators.TerminalStage;

/**
 * Checks the behavior of {@link CancelStageFactory}
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CancelStageFactoryTest extends StageTestBase {

    private final CancelStageFactory factory = new CancelStageFactory();

    @Test
    public void create() throws ExecutionException, InterruptedException {
        TerminalStage<Long, Void> terminal = factory.create(null, new Stage.Cancel() {
        });
        AtomicBoolean cancelled = new AtomicBoolean();
        List<Long> list = new ArrayList<>();
        Flowable<Long> flowable = Flowable.interval(1000, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .doOnNext(list::add)
                .doOnCancel(() -> cancelled.set(true));
        CompletionStage<Void> stage = terminal.apply(flowable);
        stage.toCompletableFuture().get();

        await().untilAtomic(cancelled, is(true));
        assertThat(list).isEmpty();
        assertThat(cancelled).isTrue();
    }

    @Test(expected = NullPointerException.class)
    public void createWithoutStage() {
        factory.create(null, null);
    }

    @Test
    public void dumb() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        AtomicBoolean done = new AtomicBoolean();
        infiniteStream().onTerminate(() -> cancelled.complete(null))
                .filter(i -> i < 3)
                .cancel()
                .run().toCompletableFuture().whenComplete((res, err) -> {
                    done.set(true);
                });
        assertThat(done).isTrue();
    }

    @Test
    public void cancelStageShouldCancelTheStage() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<Void> result = ReactiveStreams.fromPublisher(s -> s.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {
            }

            @Override
            public void cancel() {
                cancelled.complete(null);
            }
        })).cancel().run();
        await().until(cancelled::isDone);
    }

}
