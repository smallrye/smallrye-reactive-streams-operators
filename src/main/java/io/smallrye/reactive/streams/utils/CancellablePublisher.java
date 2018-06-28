package io.smallrye.reactive.streams.utils;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wrapped a source publisher and make it cancellable on demand. The cancellation happens if
 * no-one subscribed to the source publisher. This class is required for the
 * {@link io.smallrye.reactive.streams.stages.ConcatStageFactory} to enforce the reactive
 * streams rules.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CancellablePublisher<T> implements Publisher<T> {
  private final Publisher<T> source;
  private final AtomicBoolean subscribed = new AtomicBoolean();

  public CancellablePublisher(Publisher<T> delegate) {
    this.source = delegate;
  }

  @Override
  public void subscribe(Subscriber<? super T> subscriber) {
    Objects.requireNonNull(subscriber);
    if (subscribed.compareAndSet(false, true)) {
      source.subscribe(subscriber);
    } else {
      subscriber.onSubscribe(new EmptySubscription());
      subscriber.onError(new IllegalStateException("Multicast not supported"));
    }
  }

  public void cancelIfNotSubscribed() {
    if (subscribed.compareAndSet(false, true)) {
      source.subscribe(new CancellationSubscriber<>());
    }
  }
}
