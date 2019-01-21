package io.smallrye.reactive.converters.microprofile;

import io.reactivex.processors.AsyncProcessor;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

public class PublisherBuilderConverter implements ReactiveTypeConverter<PublisherBuilder> {
    @SuppressWarnings("unchecked")
    @Override
    public <X> CompletionStage<X> toCompletionStage(PublisherBuilder instance) {
        return instance.findFirst().run().thenApplyAsync(x -> ((Optional) x).orElse(null));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X> Publisher<X> toRSPublisher(PublisherBuilder instance) {
        return instance.buildRs();
    }

    @Override
    public <X> PublisherBuilder fromCompletionStage(CompletionStage<X> cs) {
        AsyncProcessor<X> processor = AsyncProcessor.create();
        cs.whenComplete((X v, Throwable e) -> {
            if (e != null) {
                processor.onError(e instanceof CompletionException ? e.getCause() : e);
            } else if (v == null) {
                processor.onError(new NullPointerException());
            } else {
                processor.onNext(v);
                processor.onComplete();
            }
        });
        return ReactiveStreams.fromPublisher(processor);
    }

    @Override
    public <X> PublisherBuilder fromPublisher(Publisher<X> publisher) {
        return ReactiveStreams.fromPublisher(publisher);
    }

    @Override
    public Class<PublisherBuilder> type() {
        return PublisherBuilder.class;
    }

    @Override
    public boolean emitItems() {
        return true;
    }

    @Override
    public boolean emitAtMostOneItem() {
        return false;
    }

    @Override
    public boolean supportNullValue() {
        return false;
    }
}
