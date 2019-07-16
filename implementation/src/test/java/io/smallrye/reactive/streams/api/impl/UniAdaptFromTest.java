package io.smallrye.reactive.streams.api.impl;

import io.reactivex.*;
import io.smallrye.reactive.streams.api.Uni;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class UniAdaptFromTest {

    @Test
    public void testCreatingFromACompletable() {
        Uni<Void> uni = Uni.from(Completable.complete());
        assertThat(uni).isNotNull();
        assertThat(uni.await().indefinitely()).isNull();
    }

    @Test
    public void testCreatingFromACompletableFromVoid() {
        Uni<Void> uni = Uni.from(Completable.error(new IOException("boom")));
        assertThat(uni).isNotNull();
        try {
            uni.await().indefinitely();
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertThat(e).hasCauseInstanceOf(IOException.class);
        }
    }

    @Test
    public void testCreatingFromASingle() {
        Uni<Integer> uni = Uni.from(Single.just(1));
        assertThat(uni).isNotNull();
        assertThat(uni.await().indefinitely()).isEqualTo(1);
    }


    @Test
    public void testCreatingFromASingleWithFailure() {
        Uni<Integer> uni = Uni.from(Single.error(new IOException("boom")));
        assertThat(uni).isNotNull();
        try {
            uni.await().indefinitely();
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertThat(e).hasCauseInstanceOf(IOException.class);
        }
    }

    @Test
    public void testCreatingFromAMaybe() {
        Uni<Integer> uni = Uni.from(Maybe.just(1));
        assertThat(uni).isNotNull();
        assertThat(uni.await().indefinitely()).isEqualTo(1);
    }

    @Test
    public void testCreatingFromAnEmptyMaybe() {
        Uni<Void> uni = Uni.from(Maybe.empty());
        assertThat(uni).isNotNull();
        assertThat(uni.await().indefinitely()).isNull();
    }

    @Test
    public void testCreatingFromAMaybeWithFailure() {
        Uni<Integer> uni = Uni.from(Maybe.error(new IOException("boom")));
        assertThat(uni).isNotNull();
        try {
            uni.await().indefinitely();
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertThat(e).hasCauseInstanceOf(IOException.class);
        }
    }

    @Test
    public void testCreatingFromAFlowable() {
        Uni<Integer> uni = Uni.from(Flowable.just(1));
        assertThat(uni).isNotNull();
        assertThat(uni.await().indefinitely()).isEqualTo(1);
    }

    @Test
    public void testCreatingFromAMultiValuedFlowable() {
        Uni<Integer> uni = Uni.from(Flowable.just(1, 2, 3));
        assertThat(uni).isNotNull();
        assertThat(uni.await().indefinitely()).isEqualTo(1);
    }

    @Test
    public void testCreatingFromAnEmptyFlowable() {
        Uni<Void> uni = Uni.from(Flowable.empty());
        assertThat(uni).isNotNull();
        assertThat(uni.await().indefinitely()).isNull();
    }

    @Test
    public void testCreatingFromAFlowableWithFailure() {
        Uni<Integer> uni = Uni.from(Flowable.error(new IOException("boom")));
        assertThat(uni).isNotNull();
        try {
            uni.await().indefinitely();
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertThat(e).hasCauseInstanceOf(IOException.class);
        }
    }

    @Test
    public void testCreatingFromAPublisherBuilder() {
        Uni<Integer> uni = Uni.from(ReactiveStreams.of(1));
        assertThat(uni).isNotNull();
        assertThat(uni.await().indefinitely()).isEqualTo(1);
    }

    @Test
    public void testCreatingFromAMultiValuedPublisherBuilder() {
        Uni<Integer> uni = Uni.from(ReactiveStreams.of(1, 2, 3));
        assertThat(uni).isNotNull();
        assertThat(uni.await().indefinitely()).isEqualTo(1);
    }

    @Test
    public void testCreatingFromAnEmptyPublisherBuilder() {
        Uni<Void> uni = Uni.from(ReactiveStreams.empty());
        assertThat(uni).isNotNull();
        assertThat(uni.await().indefinitely()).isNull();
    }

    @Test
    public void testCreatingFromAPublisherBuilderWithFailure() {
        Uni<Integer> uni = Uni.from(ReactiveStreams.failed(new IOException("boom")));
        assertThat(uni).isNotNull();
        try {
            uni.await().indefinitely();
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertThat(e).hasCauseInstanceOf(IOException.class);
        }
    }
    

    @Test
    public void testCreatingFromAMono() {
        Uni<Integer> uni = Uni.from(Mono.just(1));
        assertThat(uni).isNotNull();
        assertThat(uni.await().indefinitely()).isEqualTo(1);
    }

    @Test
    public void testCreatingFromAnEmptyMono() {
        Uni<Void> uni = Uni.from(Mono.empty());
        assertThat(uni).isNotNull();
        assertThat(uni.await().indefinitely()).isNull();
    }

    @Test
    public void testCreatingFromAMonoWithFailure() {
        Uni<Integer> uni = Uni.from(Mono.error(new IOException("boom")));
        assertThat(uni).isNotNull();
        try {
            uni.await().indefinitely();
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertThat(e).hasCauseInstanceOf(IOException.class);
        }
    }

    @Test
    public void testCreatingFromAFlux() {
        Uni<Integer> uni = Uni.from(Flux.just(1));
        assertThat(uni).isNotNull();
        assertThat(uni.await().indefinitely()).isEqualTo(1);
    }

    @Test
    public void testCreatingFromAMultiValuedFlux() {
        Uni<Integer> uni = Uni.from(Flux.just(1, 2, 3));
        assertThat(uni).isNotNull();
        assertThat(uni.await().indefinitely()).isEqualTo(1);
    }

    @Test
    public void testCreatingFromAnEmptyFlux() {
        Uni<Void> uni = Uni.from(Flux.empty());
        assertThat(uni).isNotNull();
        assertThat(uni.await().indefinitely()).isNull();
    }

    @Test
    public void testCreatingFromAFluxWithFailure() {
        Uni<Integer> uni = Uni.from(Flux.error(new IOException("boom")));
        assertThat(uni).isNotNull();
        try {
            uni.await().indefinitely();
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertThat(e).hasCauseInstanceOf(IOException.class);
        }
    }


    @Test
    public void testCreatingFromCompletionStages() {
        CompletableFuture<Integer> valued = CompletableFuture.completedFuture(1);
        CompletableFuture<Void> empty = CompletableFuture.completedFuture(null);
        CompletableFuture<Void> failed = new CompletableFuture<>();
        failed.completeExceptionally(new Exception("boom"));


        Uni<Integer> u1 = Uni.from(valued);
        Uni<Void> u2 = Uni.from(empty);
        Uni<Void> u3 = Uni.from(failed);

        assertThat(u1.await().asOptional().indefinitely()).contains(1);
        assertThat(u2.await().indefinitely()).isEqualTo(null);
        try {
            u3.await();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class).hasCauseInstanceOf(Exception.class);
        }
    }

}