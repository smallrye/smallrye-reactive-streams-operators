package io.smallrye.reactive.streams.utils;

import io.reactivex.Flowable;
import io.reactivex.processors.AsyncProcessor;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

public class CompletionStageToPublisher {

    private CompletionStageToPublisher() {
        // Avoid direct instantiation.
    }

    public static <T> Flowable<T> fromCompletionStage(CompletionStage<T> future, boolean acceptNullValue) {
        AsyncProcessor<T> processor = AsyncProcessor.create();

        Objects.requireNonNull(future).whenComplete((T v, Throwable e) -> {
            if (e != null) {
                processor.onError(e);
            } else if (v != null) {
                processor.onNext(v);
                processor.onComplete();
            } else {
                if (acceptNullValue) {
                    processor.onComplete();
                } else {
                    processor.onError(new NullPointerException("Redeemed value is `null`"));
                }
            }
        });

        return processor;
    }

}
