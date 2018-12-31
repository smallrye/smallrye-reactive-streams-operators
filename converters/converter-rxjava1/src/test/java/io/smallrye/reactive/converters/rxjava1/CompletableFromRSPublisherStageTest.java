package io.smallrye.reactive.converters.rxjava1;

import io.smallrye.reactive.converters.ReactiveTypeConverter;
import io.smallrye.reactive.converters.Registry;
import io.smallrye.reactive.converters.tck.FromRSPublisherTCK;
import org.junit.Before;
import rx.Completable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CompletableFromRSPublisherStageTest extends FromRSPublisherTCK<Completable> {

    private ReactiveTypeConverter<Completable> converter;

    @Before
    public void lookup() {
        converter = Registry.lookup(Completable.class)
                .orElseThrow(() -> new AssertionError("Completable converter should be found"));
    }

    @Override
    protected boolean supportNullValues() {
        return true;
    }

    @Override
    protected boolean emitSingleValue() {
        return false;
    }

    @Override
    protected boolean emitMultipleValues() {
        return false;
    }

    @Override
    protected ReactiveTypeConverter<Completable> converter() {
        return converter;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected String getOne(Completable instance) {
        instance.await();
        return null;
    }

    @Override
    protected Exception getFailure(Completable instance) {
        AtomicReference<Exception> reference = new AtomicReference<>();
        try {
            instance.await();
        } catch (Exception e) {
            reference.set(e);
        }
        return reference.get();
    }

    @Override
    protected List<String> getAll(Completable instance) {
        instance.await();
        return Collections.emptyList();
    }

    @Override
    protected void consume(Completable instance) {
        instance.await();
    }


}
