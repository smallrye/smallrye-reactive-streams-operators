package io.smallrye.reactive.converters.rxjava1;

import io.smallrye.reactive.converters.ReactiveTypeConverter;
import io.smallrye.reactive.converters.Registry;
import io.smallrye.reactive.converters.tck.FromCompletionStageTCK;
import org.junit.Before;
import rx.Observable;

import java.util.concurrent.atomic.AtomicReference;

public class ObservableFromCompletionStageTest extends FromCompletionStageTCK<Observable> {

    private ReactiveTypeConverter<Observable> converter;

    @Before
    public void lookup() {
        converter = Registry.lookup(Observable.class)
                .orElseThrow(() -> new AssertionError("Observable converter should be found"));
    }

    @Override
    protected ReactiveTypeConverter<Observable> converter() {
        return converter;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected String getOne(Observable instance) {
        Observable<String> single = instance.cast(String.class);
        return single.toBlocking().last();
    }

    @Override
    protected Exception getFailure(Observable instance) {
        AtomicReference<Exception> reference = new AtomicReference<>();
        try {
            instance.toBlocking().last();
        } catch (Exception e) {
            reference.set(e);
        }
        return reference.get();
    }
}
