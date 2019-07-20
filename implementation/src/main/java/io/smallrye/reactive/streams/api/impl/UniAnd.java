package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.CompositeException;
import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.smallrye.reactive.streams.api.impl.EmptySubscription.CANCELLED;

public class UniAnd<I, O> extends UniOperator<I, O> {

    private static final Object SENTINEL = new Object();
    private final Function<List<Object>, O> combinator;
    private final List<Uni<?>> unis;
    private final boolean delayError;

    public UniAnd(Uni<I> source, List<Uni<?>> others, Function<List<Object>, O> combinator, boolean delayError) {
        super(source);

        this.unis = new ArrayList<>();
        if (source != null) {
            this.unis.add(source);
        }
        this.unis.addAll(others);

        this.combinator = combinator;
        this.delayError = delayError;
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super O> subscriber) {
        AndSupervisor andSupervisor = new AndSupervisor(subscriber);
        subscriber.onSubscribe(andSupervisor);
        // Must wait until the subscriber get a subscription before subscribing to the sources.
        andSupervisor.run();
    }

    private class AndSupervisor implements UniSubscription {

        private final List<UniResult> results = new ArrayList<>();
        private final WrapperUniSubscriber<? super O> subscriber;

        AtomicBoolean cancelled = new AtomicBoolean();

        AndSupervisor(WrapperUniSubscriber<? super O> sub) {
            subscriber = sub;

            for (Uni u : unis) {
                UniResult result = new UniResult(this, u);
                results.add(result);
            }

        }

        private void run() {
            results.forEach(UniResult::subscribe);
        }

        @Override
        public void cancel() {
            if (cancelled.compareAndSet(false, true)) {
                results.forEach(UniResult::cancel);
            }
        }

        void check(UniResult res, boolean failed) {
            int incomplete = unis.size();
            // A uni has completed (with a result or a failure)

            // One of the uni failed, and we can propagate the failure immediately.
            if (failed && !delayError) {
                if (cancelled.compareAndSet(false, true)) {
                    // Cancel all subscriptions
                    results.forEach(UniResult::cancel);
                    // Invoke observer
                    subscriber.onFailure(res.failure);
                }
                return;
            }

            for (UniResult result : results) {
                if (result.failure != null || result.result != SENTINEL) {
                    incomplete = incomplete - 1;
                }
            }

            if (failed && incomplete == 0) {
                // Delay error is enabled, everybody has completed, but we have a lest a failure.
                if (cancelled.compareAndSet(false, true)) {
                    List<Throwable> throwables = results.stream()
                            .filter(u -> u.failure != null).map(u -> u.failure)
                            .collect(Collectors.toList());
                    if (throwables.size() == 1) {
                        subscriber.onFailure(throwables.get(0));
                    } else {
                        subscriber.onFailure(new CompositeException(throwables));
                    }
                }
                return;
            }

            if (!failed && incomplete == 0) {
                // We are done, everybody has completed successfully
                if (cancelled.compareAndSet(false, true)) {
                    List<Object> items = results.stream()
                            .map(u -> u.result)
                            .collect(Collectors.toList());
                    O aggregated;
                    try {
                        aggregated = combinator.apply(items);
                    } catch (Exception e) {
                        subscriber.onFailure(e);
                        return;
                    }
                    subscriber.onResult(aggregated);
                }
            }
        }
    }

    private class UniResult implements UniSubscription, UniSubscriber {

        final AtomicReference<UniSubscription> subscription = new AtomicReference<>();
        private final AndSupervisor supervisor;
        private final Uni uni;
        Object result = SENTINEL;
        Throwable failure;

        public UniResult(AndSupervisor supervisor, Uni observed) {
            this.supervisor = supervisor;
            this.uni = observed;
        }

        @Override
        public final void onSubscribe(UniSubscription sub) {
            if (!subscription.compareAndSet(null, sub)) {
                // cancelling this second subscription
                // because we already add a subscription (maybe CANCELLED)
                sub.cancel();
            }
        }

        @Override
        public final void onFailure(Throwable t) {
            UniSubscription sub = subscription.getAndSet(CANCELLED);
            if (sub == CANCELLED) {
                // Already cancelled, do nothing
                return;
            }
            this.failure = t;
            supervisor.check(this, true);
        }

        @Override
        public final void onResult(Object x) {
            Subscription sub = subscription.getAndSet(CANCELLED);
            if (sub == CANCELLED) {
                // Already cancelled, do nothing
                return;
            }
            this.result = x;
            supervisor.check(this, false);
        }

        @Override
        public void cancel() {
            Subscription sub = subscription.getAndSet(CANCELLED);
            if (sub != null) {
                sub.cancel();
            }
        }

        public void subscribe() {
            uni.subscribe().withSubscriber(this);
        }
    }
}
