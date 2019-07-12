package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.UniEmitter;
import io.smallrye.reactive.streams.api.UniSubscriber;

import java.util.Objects;
import java.util.function.Consumer;

public class UniCreate<T> extends UniImpl<T> {
    private final Consumer<UniEmitter<? super T>> consumer;

    public UniCreate(Consumer<UniEmitter<? super T>> consumer) {
        this.consumer = Objects.requireNonNull(consumer, "`consumer` cannot be `null`");
    }

    @Override
    public void subscribe(UniSubscriber<? super T> subscriber) {
        UniEmitterImpl<? super T> emitter = new UniEmitterImpl<>(subscriber);
        subscriber.onSubscribe(emitter);

        try {
            consumer.accept(emitter);
        } catch (RuntimeException e) {
            emitter.fail(e);
        }
    }
}
