package io.smallrye.reactive.streams.stages;

import org.eclipse.microprofile.reactive.streams.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.junit.Test;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class CoupledStageFactoryTest {

    private <T> PublisherBuilder<T> idlePublisher() {
        return ReactiveStreams.fromCompletionStage(new CompletableFuture<>());
    }

    @Test
    public void testDownstreamCancellation() {
        CompletableFuture<Void> publisherCancelled = new CompletableFuture<>();
        CompletableFuture<Void> downstreamCompleted = new CompletableFuture<>();

        idlePublisher()
                .via(
                        ReactiveStreams.coupled(ReactiveStreams.builder().cancel().build(),
                                idlePublisher()
                                        .onTerminate(() -> {
                                            publisherCancelled.complete(null);
                                        }).buildRs())
                ).onComplete(() -> downstreamCompleted.complete(null))
                .ignore()
                .run();
        await(publisherCancelled);
        await(downstreamCompleted);
        assertThat(publisherCancelled.isDone()).isTrue();
    }

    @Test
    public void testUpstreamCancellation() {
        CompletableFuture<Void> subscriberCompleted = new CompletableFuture<>();
        CompletableFuture<Void> upstreamCancelled = new CompletableFuture<>();
        idlePublisher()
                .onTerminate(() -> {
                    upstreamCancelled.complete(null);
                })
                .via(ReactiveStreams.coupled(ReactiveStreams.builder()
                        .onComplete(() -> {
                            subscriberCompleted.complete(null);
                        })
                        .ignore(), ReactiveStreams.empty()))
                .ignore().run();
        await(subscriberCompleted);
        await(upstreamCancelled);
        assertThat(subscriberCompleted.isDone()).isTrue();
    }

    /**
     * Wait for the given future to complete and return its value, using the configured timeout.
     */
    private <T> T await(CompletionStage<T> future) {
        try {
            return future.toCompletableFuture().get(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new RuntimeException(e.getCause());
            }
        } catch (TimeoutException e) {
            throw new RuntimeException("Future timed out after " + 500 + "ms", e);
        }
    }
}