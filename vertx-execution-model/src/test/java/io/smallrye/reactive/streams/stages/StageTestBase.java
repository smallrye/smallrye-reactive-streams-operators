package io.smallrye.reactive.streams.stages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.After;
import org.junit.Before;

import io.vertx.reactivex.core.Vertx;

/**
 * Creates and disposes the Vert.x instance.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class StageTestBase {

    private final AtomicReference<String> captured = new AtomicReference<>();
    Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
    }

    @After
    public void tearDown() {
        AtomicBoolean done = new AtomicBoolean();
        vertx.close(x -> done.set(true));
        await().untilAtomic(done, is(true));
    }

    private void captureThreadName() {
        String name = Thread.currentThread().getName();
        captured.set(name);
    }

    String getCapturedThreadName() {
        return captured.get();
    }

    <T> AsyncOperation<T> executeOnEventLoop(Callable<CompletionStage<T>> callable) {
        AsyncOperation<T> operation = new AsyncOperation<>();
        vertx.runOnContext(v -> {
            captureThreadName();
            try {
                callable.call().whenComplete(operation::complete);
            } catch (Exception e) {
                operation.complete(null, e);
            }
        });
        await().until(operation::hasCompletedOrFailed);
        return operation;

    }

    PublisherBuilder<Integer> infiniteStream() {
        return ReactiveStreams.fromIterable(() -> {
            AtomicInteger value = new AtomicInteger();
            return IntStream.generate(value::incrementAndGet).boxed().iterator();
        });
    }

    class AsyncOperation<T> {

        private T result;
        private Throwable error;
        private String callbackThread;

        synchronized void complete(T result, Throwable error) {
            if (hasCompletedOrFailed()) {
                throw new RuntimeException("Operation already completed");
            }
            this.result = result;
            this.error = error;
            this.callbackThread = Thread.currentThread().getName();
        }

        synchronized boolean hasCompletedOrFailed() {
            return result != null || error != null;
        }

        synchronized T result() {
            return result;
        }

        synchronized Throwable failure() {
            return error;
        }

        synchronized String callbackThread() {
            return callbackThread;
        }

        void assertSuccess(T expected) {
            assertThat(result()).isEqualTo(expected);
            assertThat(failure()).isNull();
            assertThat(callbackThread()).isEqualTo(getCapturedThreadName());
        }

        void assertFailure(String message) {
            assertThat(callbackThread()).isEqualTo(getCapturedThreadName());
            assertThat(result()).isNull();
            assertThat(failure()).hasMessageContaining(message);
        }
    }
}
