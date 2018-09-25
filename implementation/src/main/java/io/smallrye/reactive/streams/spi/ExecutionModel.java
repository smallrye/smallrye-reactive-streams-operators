package io.smallrye.reactive.streams.spi;

import io.reactivex.Flowable;

@FunctionalInterface
public interface ExecutionModel {

    Flowable transform(Flowable input);

}
