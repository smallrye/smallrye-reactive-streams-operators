package io.smallrye.reactive.streams.spi;

import java.util.function.UnaryOperator;

import io.reactivex.Flowable;

@FunctionalInterface
public interface ExecutionModel extends UnaryOperator<Flowable> {

}
