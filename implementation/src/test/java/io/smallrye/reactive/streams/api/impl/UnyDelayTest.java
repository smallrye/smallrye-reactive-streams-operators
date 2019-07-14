package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.junit.After;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class UnyDelayTest {


    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    private Uni<Void> delayed = Uni.empty().delay(Duration.ofMillis(100), executor);


    @After
    public void shutdown() {
        executor.shutdown();
    }

    @Test(expected = NullPointerException.class)
    public void testWithNullDuration() {
        Uni.of(1).delay(null, executor);
    }

    @Test(expected = NullPointerException.class)
    public void testWithNullExecutor() {
        Uni.of(1).delay(Duration.ofHours(1), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithNegativeDuration() {
        Uni.of(1).delay(Duration.ofDays(-1), executor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithZeroAsDuration() {
        Uni.of(1).delay(Duration.ZERO, executor);
    }

    @Test
    public void testDelayOnResult() {
        long begin = System.currentTimeMillis();
        AssertSubscriber<Void> subscriber = AssertSubscriber.create();
        delayed.subscribe(subscriber);
        subscriber.await();
        long end = System.currentTimeMillis();
        assertThat(end - begin).isGreaterThan(100);
        subscriber.assertCompletedSuccessfully().assertResult(null);
        assertThat(subscriber.getOnResultThreadName()).isNotEqualTo(Thread.currentThread().getName());
    }

    @Test
    public void testDelayOnFailure() {
        long begin = System.currentTimeMillis();
        AssertSubscriber<Void> subscriber = AssertSubscriber.create();
        Uni.<Void>failed(new Exception("boom")).delay(Duration.ofMillis(100), executor).subscribe(subscriber);
        subscriber.await();
        long end = System.currentTimeMillis();
        assertThat(end - begin).isGreaterThan(100);
        subscriber.assertCompletedWithFailure().assertFailure(Exception.class, "boom");
        assertThat(subscriber.getOnFailureThreadName()).isNotEqualTo(Thread.currentThread().getName());
    }

    @Test
    public void testThatNothingIsSubmittedOnImmediateCancellation() {
        AtomicBoolean called = new AtomicBoolean();
        executor.shutdown();
        executor = new ScheduledThreadPoolExecutor(4) {
            @Override
            public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
                called.set(true);
                return super.schedule(command, delay, unit);
            }
        };

        AssertSubscriber<Integer> subscriber = new AssertSubscriber<>(true);
        Uni.of(1).delay(Duration.ofMillis(100), executor).subscribe(subscriber);
        subscriber.assertNotCompleted();
        assertThat(called).isFalse();
    }

    @Test
    public void testRejectedScheduling() {
        executor.shutdown();
        AssertSubscriber<Integer> subscriber = new AssertSubscriber<>();
        Uni.of(1).delay(Duration.ofMillis(100), executor).subscribe(subscriber);
        subscriber.assertCompletedWithFailure().assertFailure(RejectedExecutionException.class, "");
    }

    @Test
    public void testCancellationHappeningDuringTheWaitingTime() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ScheduledFuture<?>> future = new AtomicReference<>();
        executor.shutdown();

        executor = new ScheduledThreadPoolExecutor(4) {
            @Override
            public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
                ScheduledFuture<?> schedule = super.schedule(() -> {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    command.run();

                }, delay, unit);
                future.set(schedule);
                return schedule;
            }
        };

        AssertSubscriber<Integer> subscriber = new AssertSubscriber<>();
        Uni.of(1).delay(Duration.ofMillis(100), executor).subscribe(subscriber);
        subscriber.cancel();
        latch.countDown();

        await().until(() -> future.get() != null  && future.get().isCancelled());
        subscriber.assertNotCompleted();
    }

    @Test
    public void testWithMultipleDelays() throws InterruptedException {
        AtomicLong counter = new AtomicLong();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Uni.empty().delay(Duration.ofMillis(50), executor).subscribe(v -> counter.incrementAndGet(), failure::set);
        Uni.empty().delay(Duration.ofMillis(200), executor).subscribe(v -> counter.incrementAndGet(), failure::set);
        Uni.empty().delay(Duration.ofMillis(400), executor).subscribe(v -> counter.incrementAndGet(), failure::set);
        Uni.empty().delay(Duration.ofMillis(800), executor).subscribe(v -> counter.incrementAndGet(), failure::set);

        assertThat(counter.intValue()).isEqualTo(0);
        assertThat(failure.get()).isNull();

        Thread.sleep(250);
        assertThat(counter.intValue()).isEqualTo(2);
        assertThat(failure.get()).isNull();

        Thread.sleep(1000);
        assertThat(counter.intValue()).isEqualTo(4);
        assertThat(failure.get()).isNull();
    }
}