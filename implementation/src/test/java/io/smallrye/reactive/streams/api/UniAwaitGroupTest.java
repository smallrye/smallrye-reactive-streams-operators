package io.smallrye.reactive.streams.api;

import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

public class UniAwaitGroupTest {

    @Test(timeout = 1000)
    public void testAwaitingOnAnAlreadyResolvedUni() {
        assertThat(Uni.of(1).await().indefinitely()).isEqualTo(1);
    }

    @Test(timeout = 100)
    public void testAwaitingOnAnAlreadyResolvedWitNullUni() {
        assertThat(Uni.from().nullValue().await().indefinitely()).isNull();
    }

    @Test(timeout = 100)
    public void testAwaitingOnAnAlreadyFailedUni() {
        try {
            Uni.from().failure(new IOException("boom")).await().indefinitely();
            fail("Exception expected");
        } catch (CompletionException e) {
            assertThat(e).hasCauseInstanceOf(IOException.class).hasMessageEndingWith("boom");
        }
    }

    // Uni.from().failure before onTimeout

    @Test(timeout = 1000)
    public void testAwaitingOnAnAsyncUni() {
        assertThat(
                 Uni.from().emitter(emitter -> new Thread(() -> emitter.success(1)).start()).await().indefinitely()
        ).isEqualTo(1);
    }

    @Test(timeout = 1000)
    public void testAwaitingOnAnAsyncFailingUni() {
        try {
             Uni.from().emitter(emitter -> new Thread(() -> emitter.fail(new IOException("boom"))).start()).await().indefinitely();
            fail("Exception expected");
        } catch (CompletionException e) {
            assertThat(e).hasCauseInstanceOf(IOException.class).hasMessageEndingWith("boom");
        }
    }

    @Test(timeout = 100)
    public void testAwaitWithTimeOut() {
        assertThat(Uni.of(1).await().atMost(Duration.ofMillis(1000))).isEqualTo(1);
    }

    @Test(timeout = 100, expected = TimeoutException.class)
    public void testTimeout() {
        Uni.from().nothing().await().atMost(Duration.ofMillis(10));
    }

    @Test(timeout = 5000)
    public void testInterruptedTimeout() {
        AtomicBoolean awaiting = new AtomicBoolean();
        AtomicReference<RuntimeException> exception = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                awaiting.set(true);
                Uni.from().nothing().await().atMost(Duration.ofMillis(1000));
            } catch (RuntimeException e) {
                exception.set(e);
            }
        });
        thread.start();
        await().untilTrue(awaiting);
        thread.interrupt();
        await().until(() -> exception.get() != null);
        assertThat(exception.get()).hasCauseInstanceOf(InterruptedException.class);
    }

    @Test
    public void testAwaitAsOptionalWithResult() {
        assertThat(
                 Uni.from().emitter(emitter -> new Thread(() -> emitter.success(1)).start()).await().asOptional().indefinitely()
        ).contains(1);
    }

    @Test
    public void testAwaitAsOptionalWithFailure() {
        try {
             Uni.from().emitter(emitter -> new Thread(() -> emitter.fail(new IOException("boom"))).start())
                    .await().asOptional().indefinitely();
            fail("Exception expected");
        } catch (CompletionException e) {
            assertThat(e).hasCauseInstanceOf(IOException.class).hasMessageEndingWith("boom");
        }
    }

    @Test
    public void testAwaitAsOptionalWithNull() {
        assertThat(
                 Uni.from().emitter(emitter -> new Thread(() -> emitter.success(null)).start()).await().asOptional().indefinitely()
        ).isEmpty();
    }

    @Test
    public void testAwaitAsOptionalWithTimeout() {
        assertThat(
                 Uni.from().emitter(emitter -> new Thread(() -> emitter.success(1)).start()).await().asOptional().atMost(Duration.ofMillis(1000))
        ).contains(1);
    }

    @Test(timeout = 100, expected = TimeoutException.class)
    public void testTimeoutAndOptional() {
        Uni.from().nothing().await().asOptional().atMost(Duration.ofMillis(10));
    }

}