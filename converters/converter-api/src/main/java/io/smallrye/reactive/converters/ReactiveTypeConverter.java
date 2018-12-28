package io.smallrye.reactive.converters;

import org.reactivestreams.Publisher;

import java.util.concurrent.CompletionStage;

public interface ReactiveTypeConverter<T> {

    <X> CompletionStage<X> toCompletionStage(T instance);

    <X>Publisher<X> toRSPublisher(T instance);

    <X> T fromCompletionStage(CompletionStage<X> cs);

    <X> T fromPublisher(Publisher<X> publisher);

    Class<T> type();

}
