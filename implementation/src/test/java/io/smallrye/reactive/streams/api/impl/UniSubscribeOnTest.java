package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class UniSubscribeOnTest {

    @Test
    public void testSubscribeOnWithSupplier() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        Uni.from().value(() -> 1)
                .subscribeOn(ForkJoinPool.commonPool())
                .subscribe().withSubscriber(ts);
        ts.await().assertResult(1);
        assertThat(ts.getOnSubscribeThreadName()).isNotEqualTo(Thread.currentThread().getName());
    }

    @Test
    public void testWithWithImmediateValue() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();

        Uni.of(1)
                .subscribeOn(ForkJoinPool.commonPool())
                .subscribe().withSubscriber(ts);

        ts.await().assertResult(1);
        assertThat(ts.getOnSubscribeThreadName()).isNotEqualTo(Thread.currentThread().getName());
    }

    @Test
    public void testWithTimeout() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();

        Uni.from().value(() -> {
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                // ignored
            }
            return 0;
        })
                .onTimeout().of(Duration.ofMillis(100)).recover().withUni(Uni.from().value(() -> 1))
                .subscribeOn(Infrastructure.getDefaultExecutor())
                .subscribe().withSubscriber(ts);

        ts.await().assertResult(1);
    }

    @Test
    public void callableEvaluatedTheRightTime() {
        AtomicInteger count = new AtomicInteger();

        Uni<Integer> uni = Uni.from().value(count::incrementAndGet)
                .subscribeOn(ForkJoinPool.commonPool());

        assertThat(count).hasValue(0);
        uni.subscribe().withSubscriber(AssertSubscriber.create()).await();
        assertThat(count).hasValue(1);
    }

    @Test
    public void testWithFailure() {
        Uni.from().<Void>failure(new IOException("boom"))
                .subscribeOn(Infrastructure.getDefaultExecutor())
                .subscribe().withSubscriber(AssertSubscriber.create())
                .await()
                .assertFailure(IOException.class, "boom");
    }

}