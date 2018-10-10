package io.smallrye.reactive.streams.spi;

import io.reactivex.Flowable;

import java.util.function.UnaryOperator;

@FunctionalInterface
public interface ExecutionModel extends UnaryOperator<Flowable> {
    
}
