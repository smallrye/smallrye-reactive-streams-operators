package io.smallrye.reactive.converters.reactor;

import io.smallrye.reactive.converters.ReactiveTypeConverter;
import io.smallrye.reactive.converters.Registry;
import io.smallrye.reactive.converters.tck.FromRSPublisherTCK;
import org.junit.Before;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class FluxFromRSPublisherTest extends FromRSPublisherTCK<Flux> {

    private ReactiveTypeConverter<Flux> converter;

    @Before
    public void lookup() {
        converter = Registry.lookup(Flux.class)
                .orElseThrow(() -> new AssertionError("Flux converter should be found"));
    }

    @Override
    protected ReactiveTypeConverter<Flux> converter() {
        return converter;
    }

    @Override
    protected String getOne(Flux instance) {
        return (String) instance.blockFirst();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Exception getFailure(Flux instance) {
        AtomicReference<Exception> reference = new AtomicReference<>();
        try {
            instance.toIterable().forEach(x -> {
            });
        } catch (Exception e) {
            reference.set(e);
        }
        return reference.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<String> getAll(Flux instance) {
        return (List<String>) instance.collectList().block();
    }

    @Override
    protected void consume(Flux instance) {
        instance.last().block();
    }

    @Override
    protected boolean supportNullValues() {
        return true;
    }

    @Override
    protected boolean emitSingleValue() {
        return true;
    }

    @Override
    protected boolean emitMultipleValues() {
        return true;
    }
}
