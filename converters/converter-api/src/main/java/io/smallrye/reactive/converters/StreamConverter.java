package io.smallrye.reactive.converters;

import org.reactivestreams.Publisher;

public interface StreamConverter<T> {

    Publisher toRsPublisher(T instance);

    T fromPublisher(Publisher cs);

    Class<T> type();

}
