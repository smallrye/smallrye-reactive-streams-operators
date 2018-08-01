package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.smallrye.reactive.streams.Engine;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of {@link Stage.Cancel}. It subscribes and disposes the stream immediately.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CancelStageFactory implements TerminalStageFactory<Stage.Cancel> {

  @Override
  public <IN, OUT> TerminalStage<IN, OUT> create(Engine engine, Stage.Cancel stage) {
    Objects.requireNonNull(stage);
    return flowable -> {
      flowable.subscribe(new Subscriber<IN>() {
        private Subscription subscription;
        @Override
        public void onSubscribe(Subscription s) {
          subscription = s;
          s.cancel();
        }

        @Override
        public void onNext(IN in) {
          // Do nothing.
        }

        @Override
        public void onError(Throwable t) {
          // Do nothing.
        }

        @Override
        public void onComplete() {
          // Do nothing.
        }
      });
      return CompletableFuture.completedFuture(null);
    };
  }
}
