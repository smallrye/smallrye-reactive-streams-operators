package io.smallrye.reactive.streams.api.impl;

import io.reactivex.*;
import io.reactivex.subscribers.TestSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class UniAdaptToTest {

    @Test
    public void testCreatingACompletable() {
        Completable completable = Uni.of(1).to(Completable.class);
        assertThat(completable).isNotNull();
        completable.test().assertComplete();
    }

    @Test
    public void testThatSubscriptionOnCompletableProducesTheValue() {
        AtomicBoolean called = new AtomicBoolean();
        Completable completable =  Uni.from().deferred(() -> {
            called.set(true);
            return Uni.of(2);
        }).to(Completable.class);

        assertThat(completable).isNotNull();
        assertThat(called).isFalse();
        completable.test().assertComplete();
        assertThat(called).isTrue();
    }

    @Test
    public void testCreatingACompletableFromVoid() {
        Completable completable = Uni.from().nullValue().to(Completable.class);
        assertThat(completable).isNotNull();
        completable.test().assertComplete();
    }

    @Test
    public void testCreatingACompletableWithFailure() {
        Completable completable = Uni.from().failure(new IOException("boom")).to(Completable.class);
        assertThat(completable).isNotNull();
        completable.test().assertError(e -> {
            assertThat(e).hasMessage("boom").isInstanceOf(IOException.class);
            return true;
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingASingle() {
        Single<Integer> single = Uni.of(1).to(Single.class);
        assertThat(single).isNotNull();
        single.test()
                .assertValue(1)
                .assertComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingASingleFromNull() {
        Single<Integer> single = Uni.from().nullValue().to(Single.class);
        assertThat(single).isNotNull();
        single
                .test()
                .assertError(NoSuchElementException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingASingleWithFailure() {
        Single<Integer> single = Uni.from().failure(new IOException("boom")).to(Single.class);
        assertThat(single).isNotNull();
        single.test().assertError(e -> {
            assertThat(e).hasMessage("boom").isInstanceOf(IOException.class);
            return true;
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAMaybe() {
        Maybe<Integer> maybe = Uni.of(1).to(Maybe.class);
        assertThat(maybe).isNotNull();
        maybe.test()
                .assertValue(1)
                .assertComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAMaybeFromNull() {
        Maybe<Integer> maybe = Uni.from().nullValue().to(Maybe.class);
        assertThat(maybe).isNotNull();
        maybe
                .test()
                .assertComplete()
                .assertNoValues();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAMaybeWithFailure() {
        Maybe<Integer> maybe = Uni.from().failure(new IOException("boom")).to(Maybe.class);
        assertThat(maybe).isNotNull();
        maybe.test().assertError(e -> {
            assertThat(e).hasMessage("boom").isInstanceOf(IOException.class);
            return true;
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAnObservable() {
        Observable<Integer> observable = Uni.of(1).to(Observable.class);
        assertThat(observable).isNotNull();
        observable.test()
                .assertValue(1)
                .assertComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAnObservableFromNull() {
        Observable<Integer> observable = Uni.from().nullValue().to(Observable.class);
        assertThat(observable).isNotNull();
        observable
                .test()
                .assertComplete()
                .assertNoValues();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAnObservableWithFailure() {
        Observable<Integer> observable = Uni.from().failure(new IOException("boom")).to(Observable.class);
        assertThat(observable).isNotNull();
        observable.test().assertError(e -> {
            assertThat(e).hasMessage("boom").isInstanceOf(IOException.class);
            return true;
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAFlowable() {
        Flowable<Integer> flowable = Uni.of(1).to(Flowable.class);
        assertThat(flowable).isNotNull();
        flowable.test()
                .assertValue(1)
                .assertComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAFlowableWithRequest() {
        AtomicBoolean called = new AtomicBoolean();
        Flowable<Integer> flowable =  Uni.from().deferred(() -> {
            called.set(true);
            return Uni.of(1);
        }).to(Flowable.class);
        assertThat(flowable).isNotNull();
        TestSubscriber<Integer> test = flowable.test(0);
        assertThat(called).isFalse();
        test.assertNoValues().assertSubscribed();
        test.request(2);
        test.assertValue(1).assertComplete();
        assertThat(called).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAFlowableFromNull() {
        Flowable<Integer> flowable = Uni.from().nullValue().to(Flowable.class);
        assertThat(flowable).isNotNull();
        flowable
                .test()
                .assertComplete()
                .assertNoValues();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAFlowableWithFailure() {
        Flowable<Integer> flowable = Uni.from().failure(new IOException("boom")).to(Flowable.class);
        assertThat(flowable).isNotNull();
        flowable.test().assertError(e -> {
            assertThat(e).hasMessage("boom").isInstanceOf(IOException.class);
            return true;
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAPublisherBuilder() {
        PublisherBuilder<Integer> builder = Uni.of(1).to(PublisherBuilder.class);
        assertThat(builder).isNotNull();
        Flowable.fromPublisher(builder.buildRs()).test()
                .assertValue(1)
                .assertComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAPublisherBuilderWithRequest() {
        AtomicBoolean called = new AtomicBoolean();
        PublisherBuilder<Integer> builder =  Uni.from().deferred(() -> {
            called.set(true);
            return Uni.of(1);
        }).to(PublisherBuilder.class);
        assertThat(builder).isNotNull();
        TestSubscriber<Integer> test = Flowable.fromPublisher(builder.buildRs()).test(0);
        assertThat(called).isFalse();
        test.assertNoValues().assertSubscribed();
        test.request(2);
        test.assertValue(1).assertComplete();
        assertThat(called).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAPublisherBuilderFromNull() {
        PublisherBuilder<Integer> builder = Uni.from().nullValue().to(PublisherBuilder.class);
        assertThat(builder).isNotNull();
        Flowable.fromPublisher(builder.buildRs()).test()
                .assertComplete()
                .assertNoValues();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAPublisherBuilderWithFailure() {
        PublisherBuilder<Integer> builder = Uni.from().failure(new IOException("boom")).to(PublisherBuilder.class);
        assertThat(builder).isNotNull();
        Flowable.fromPublisher(builder.buildRs()).test()
                .assertError(e -> {
                    assertThat(e).hasMessage("boom").isInstanceOf(IOException.class);
                    return true;
                });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAFlux() {
        Flux<Integer> flux = Uni.of(1).to(Flux.class);
        assertThat(flux).isNotNull();
        assertThat(flux.blockFirst()).isEqualTo(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAFluxFromNull() {
        Flux<Integer> flux = Uni.from().nullValue().to(Flux.class);
        assertThat(flux).isNotNull();
        assertThat(flux.blockFirst()).isNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAFluxWithFailure() {
        Flux<Integer> flux = Uni.from().failure(new IOException("boom")).to(Flux.class);
        assertThat(flux).isNotNull();
        try {
            flux.blockFirst();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class).hasCauseInstanceOf(IOException.class);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAMono() {
        Mono<Integer> mono = Uni.of(1).to(Mono.class);
        assertThat(mono).isNotNull();
        assertThat(mono.block()).isEqualTo(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAMonoFromNull() {
        Mono<Integer> mono = Uni.from().nullValue().to(Mono.class);
        assertThat(mono).isNotNull();
        assertThat(mono.block()).isNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAMonoWithFailure() {
        Mono<Integer> mono = Uni.from().failure(new IOException("boom")).to(Mono.class);
        assertThat(mono).isNotNull();
        try {
            mono.block();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class).hasCauseInstanceOf(IOException.class);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingCompletionStages() {
        Uni<Integer> valued = Uni.of(1);
        Uni<Void> empty = Uni.from().nullValue();
        Uni<Void> failure = Uni.from().failure(new Exception("boom"));

        CompletionStage<Integer> stage1 = valued.to(CompletionStage.class);
        CompletionStage<Void> stage2 = empty.to(CompletionStage.class);
        CompletionStage<Void> stage3 = failure.to(CompletionStage.class);

        assertThat(stage1).isCompletedWithValue(1);
        assertThat(stage2).isCompletedWithValue(null);
        assertThat(stage3).isCompletedExceptionally();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingCompletableFutures() {
        Uni<Integer> valued = Uni.of(1);
        Uni<Void> empty = Uni.from().nullValue();
        Uni<Void> failure = Uni.from().failure(new Exception("boom"));

        CompletableFuture<Integer> stage1 = valued.to(CompletableFuture.class);
        CompletableFuture<Void> stage2 = empty.to(CompletableFuture.class);
        CompletableFuture<Void> stage3 = failure.to(CompletableFuture.class);

        assertThat(stage1).isCompletedWithValue(1);
        assertThat(stage2).isCompletedWithValue(null);
        assertThat(stage3).isCompletedExceptionally();
    }
}