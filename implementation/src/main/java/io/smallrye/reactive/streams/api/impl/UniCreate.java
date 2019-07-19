package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.UniEmitter;

import java.util.function.Consumer;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class UniCreate<T> extends DefaultUni<T> {
    private final Consumer<UniEmitter<? super T>> consumer;

    public UniCreate(Consumer<UniEmitter<? super T>> consumer) {
        this.consumer = nonNull(consumer, "consumer");
    }

    @Override
    public void subscribing(WrapperUniSubscriber<? super T> subscriber) {
        DefaultUniEmitter<? super T> emitter = new DefaultUniEmitter<>(subscriber);
        subscriber.onSubscribe(emitter);

        try {
            consumer.accept(emitter);
        } catch (RuntimeException e) {
            emitter.fail(e);
        }
    }
}
