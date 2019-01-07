package io.smallrye.reactive.converters.rxjava2;

import io.reactivex.Flowable;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import io.smallrye.reactive.converters.Registry;
import io.smallrye.reactive.converters.tck.FromCompletionStageTCK;
import org.junit.Before;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

public class FlowableFromCompletionStageTest extends FromCompletionStageTCK<Flowable> {

    private ReactiveTypeConverter<Flowable> converter;

    @Before
    public void lookup() {
        converter = Registry.lookup(Flowable.class)
                .orElseThrow(() -> new AssertionError("Flowable converter should be found"));
    }

    @Override
    protected ReactiveTypeConverter<Flowable> converter() {
        return converter;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected String getOne(Flowable instance) {
        Flowable<String> single = instance.cast(String.class);
        try {
            return single.blockingLast();
        } catch (NoSuchElementException e) {
            return null;
        }

    }

    @Override
    protected Exception getFailure(Flowable instance) {
        AtomicReference<Exception> reference = new AtomicReference<>();
        try {
            //noinspection ResultOfMethodCallIgnored
            instance.blockingLast();
        } catch (Exception e) {
            reference.set(e);
        }
        return reference.get();
    }
}
