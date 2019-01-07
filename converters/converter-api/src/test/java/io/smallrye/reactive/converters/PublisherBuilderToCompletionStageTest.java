package io.smallrye.reactive.converters;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.smallrye.reactive.converters.tck.ToCompletionStageTCK;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.Before;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PublisherBuilderToCompletionStageTest extends ToCompletionStageTCK<PublisherBuilder> {


    @Before
    public void lookup() {
        converter = Registry.lookup(PublisherBuilder.class)
                .orElseThrow(() -> new AssertionError("PublisherBuilder converter should be found"));
    }

    private ReactiveTypeConverter<PublisherBuilder> converter;

    @Override
    protected Optional<PublisherBuilder> createInstanceEmittingASingleValueImmediately(String value) {
        return Optional.of(ReactiveStreams.of(value));
    }

    @Override
    protected Optional<PublisherBuilder> createInstanceEmittingASingleValueAsynchronously(String value) {
        return Optional.of(ReactiveStreams.fromPublisher(Single.just(value)
                .delay(10, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.computation()).toFlowable()));
    }

    @Override
    protected PublisherBuilder createInstanceFailingImmediately(RuntimeException e) {
        return ReactiveStreams.failed(e);
    }

    @Override
    protected PublisherBuilder createInstanceFailingAsynchronously(RuntimeException e) {
        return ReactiveStreams.fromPublisher(Single.just("x")
                .delay(10, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.computation())
                .map(s -> {
                    throw e;
                })
                .toFlowable());
    }

    @Override
    protected Optional<PublisherBuilder> createInstanceEmittingANullValueImmediately() {
        return Optional.empty();
    }

    @Override
    protected Optional<PublisherBuilder> createInstanceEmittingANullValueAsynchronously() {
        return Optional.empty();
    }

    @Override
    protected Optional<PublisherBuilder> createInstanceEmittingMultipleValues(String... values) {
        return Optional.of(ReactiveStreams.of(values));
    }

    @Override
    protected Optional<PublisherBuilder> createInstanceEmittingAMultipleValuesAndFailure(String v1, String v2, RuntimeException e) {
        PublisherBuilder<String> builder = ReactiveStreams.of(v1, v2, "SENTINEL")
                .map(s -> {
                    if ("SENTINEL".equalsIgnoreCase(s)) {
                        throw e;
                    }
                    return s;
                });
        return Optional.of(builder);
    }

    @Override
    protected Optional<PublisherBuilder> createInstanceCompletingImmediately() {
        return Optional.of(ReactiveStreams.empty());
    }

    @Override
    protected Optional<PublisherBuilder> createInstanceCompletingAsynchronously() {
        return Optional.of(ReactiveStreams.empty());
    }

    @Override
    protected Optional<PublisherBuilder> never() {
        return Optional.of(ReactiveStreams.fromPublisher(Flowable.never()));
    }

    @Override
    protected Optional<PublisherBuilder> empty() {
        return Optional.of(ReactiveStreams.empty());
    }

    @Override
    protected ReactiveTypeConverter<PublisherBuilder> converter() {
        return converter;
    }
}
