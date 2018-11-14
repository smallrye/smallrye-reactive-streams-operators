package io.smallrye.reactive.streams.operators;

import io.smallrye.reactive.streams.Engine;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

public class ProcessorOperator<T extends Stage> extends Operator<T> {

    private ProcessingStageFactory<T> factory;

    public ProcessorOperator(Class<T> clazz, ProcessingStageFactory<T> factory) {
        super(clazz);
        this.factory = factory;
    }

    public <I, O> ProcessingStage<I, O> create(Engine engine, T stage) {
        return factory.create(engine, stage);
    }
}
