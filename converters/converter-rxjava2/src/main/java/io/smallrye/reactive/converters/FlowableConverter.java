package io.smallrye.reactive.converters;

import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

public class FlowableConverter implements StreamConverter<Flowable> {

    @Override
    public Publisher toRsPublisher(Flowable instance) {
        return instance;
    }

    @Override
    public Flowable fromPublisher(Publisher cs) {
        return Flowable.fromPublisher(cs);
    }

    @Override
    public Class<Flowable> type() {
        return Flowable.class;
    }
}
