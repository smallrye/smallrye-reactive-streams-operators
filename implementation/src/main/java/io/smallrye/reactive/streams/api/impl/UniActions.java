package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniSubscriber;
import io.smallrye.reactive.streams.api.UniSubscription;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class UniActions<T> extends UniOperator<T, T> {


    private final Consumer<? super UniSubscription> onSubscription;
    private final Consumer<? super T> onResult;
    private final Consumer<Throwable> onFailure;
    private final Runnable onCancellation;
    private final BiConsumer<? super T, Throwable> onTerminate;

    public UniActions(Uni<? extends T> source,
                      Consumer<? super UniSubscription> onSubscription,
                      Consumer<? super T> onResult,
                      Consumer<Throwable> onFailure,
                      Runnable onCancellation,
                      BiConsumer<? super T, Throwable> onTerminate) {
        super(source);
        this.onSubscription = onSubscription;
        this.onResult = onResult;
        this.onFailure = onFailure;
        this.onCancellation = onCancellation;
        this.onTerminate = onTerminate;
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super T> subscriber) {
        source().subscribe().withSubscriber(new UniSubscriber<T>() {
            @Override
            public void onSubscribe(UniSubscription subscription) {
                UniSubscription sub  = () -> {
                    subscription.cancel();
                    if (onCancellation != null) {
                        onCancellation.run();
                    }
                };

                if (onSubscription != null) {
                    try {
                        onSubscription.accept(subscription);
                    } catch (Throwable e) {
                        subscriber.onSubscribe(EmptySubscription.INSTANCE);
                        subscriber.onFailure(e);
                        return;
                    }
                }

                subscriber.onSubscribe(sub);
            }

            @Override
            public void onResult(T result) {
                if (onResult != null) {
                    try {
                        onResult.accept(result);
                    } catch (Throwable e) {
                        subscriber.onFailure(e);
                        callOnTerminate(null, e);
                        return;
                    }
                }

                subscriber.onResult(result);

                callOnTerminate(result, null);
            }

            @Override
            public void onFailure(Throwable failure) {
                if (onFailure != null) {
                    try {
                        onFailure.accept(failure);
                    } catch (Throwable e) {
                        subscriber.onFailure(e);
                        callOnTerminate(null, e);
                        return;
                    }
                }

                subscriber.onFailure(failure);

                callOnTerminate(null, failure);
            }
        });
    }

    private void callOnTerminate(T result, Throwable failure) {
        if (onTerminate != null) {
            onTerminate.accept(result, failure);
        }
    }
}
