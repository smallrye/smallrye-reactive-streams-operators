package io.smallrye.reactive.converters;

import io.smallrye.reactive.converters.tck.FromCompletionStageTCK;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.junit.Before;

import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;

public class PublisherBuilderFromCompletionStageTest extends FromCompletionStageTCK<PublisherBuilder> {


    @Before
    public void lookup() {
        converter = Registry.lookup(PublisherBuilder.class)
                .orElseThrow(() -> new AssertionError("PublisherBuilder converter should be found"));
    }

    private ReactiveTypeConverter<PublisherBuilder> converter;

    @Override
    protected ReactiveTypeConverter<PublisherBuilder> converter() {
        return converter;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected String getOne(PublisherBuilder instance) {
        return ((Optional<String>) instance.findFirst().run().toCompletableFuture().join()).orElse(null);
    }

    @Override
    protected Exception getFailure(PublisherBuilder instance) {
        AtomicReference<Throwable> reference = new AtomicReference<>();
        try {
            instance.forEach(x -> {
                // Do nothing.
            }).run().toCompletableFuture().join();
        } catch (Exception e) {
            reference.set((e instanceof CompletionException ? e.getCause() : e));
        }
        return (Exception) reference.get();
    }

    @Override
    protected boolean supportNullValues() {
        return false;
    }

    @Override
    protected boolean emitValues() {
        return true;
    }
}
