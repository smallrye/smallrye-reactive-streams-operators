package io.smallrye.reactive.converters;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import org.reactivestreams.Publisher;

public class ObservableConverter implements StreamConverter<Observable> {

    @Override
    public Publisher toRsPublisher(Observable instance) {
        return instance.toFlowable(BackpressureStrategy.MISSING);
    }

    @Override
    public Observable fromPublisher(Publisher cs) {
        return Observable.fromPublisher(cs);
    }

    @Override
    public Class<Observable> type() {
        return Observable.class;
    }
}
