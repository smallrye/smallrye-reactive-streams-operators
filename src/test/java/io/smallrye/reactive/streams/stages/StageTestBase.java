package io.smallrye.reactive.streams.stages;

import io.smallrye.reactive.streams.Engine;
import org.junit.After;
import org.junit.Before;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Creates and disposes the engine.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class StageTestBase {

  Engine engine;

  private final AtomicReference<String> captured = new AtomicReference<>();

  @Before
  public void setUp() {
    engine = new Engine();
  }

  @After
  public void tearDown() {
    engine.close();
  }

  private void captureThreadName() {
    String name = Thread.currentThread().getName();
    captured.set(name);
  }

  String getCapturedThreadName() {
    return captured.get();
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

    void checkSuccess(T expected) {
      assertThat(result()).isEqualTo(expected);
      assertThat(failure()).isNull();
      assertThat(callbackThread()).isEqualTo(getCapturedThreadName());
    }

    void checkFailure(String message) {
      assertThat(callbackThread()).isEqualTo(getCapturedThreadName());
      assertThat(result()).isNull();
      assertThat(failure()).hasMessageContaining(message);
    }
  }

  <T> AsyncOperation<T> executeOnEventLoop(Callable<CompletionStage<T>> callable) {
    AsyncOperation<T> operation = new AsyncOperation<>();
    engine.vertx().runOnContext(v -> {
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
}
