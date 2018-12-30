package io.smallrye.reactive.converters.rxjava2;

import io.smallrye.reactive.converters.ReactiveTypeConverter;
import io.smallrye.reactive.converters.Registry;
import io.smallrye.reactive.converters.tck.ToCompletionStageTCK;
import org.junit.Before;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CompletableToCompletionStageTest extends ToCompletionStageTCK<Completable> {

    private static final int DELAY = 10;
    private ReactiveTypeConverter<Completable> converter;

    @Before
    public void lookup() {
        converter = Registry.lookup(Completable.class)
                .orElseThrow(() -> new AssertionError("Completable converter should be found"));
    }

    @Override
    protected Optional<Completable> createInstanceEmittingASingleValueImmediately(String value) {
        return Optional.empty();
    }

    @Override
    protected Optional<Completable> createInstanceEmittingASingleValueAsynchronously(String value) {
        return Optional.empty();
    }

    @Override
    protected Completable createInstanceFailingImmediately(RuntimeException e) {
        return Completable.error(e);
    }

    @Override
    protected Completable createInstanceFailingAsynchronously(RuntimeException e) {
        return Completable.complete()
                .delay(DELAY, TimeUnit.MILLISECONDS)
                .andThen(Completable.error(e))
                .observeOn(Schedulers.computation());
    }

    @Override
    protected Optional<Completable> createInstanceEmittingANullValueImmediately() {
        return Optional.empty();
    }

    @Override
    protected Optional<Completable> createInstanceEmittingANullValueAsynchronously() {
        return Optional.empty();
    }

    @Override
    protected Optional<Completable> createInstanceEmittingAMultipleValues(String... values) {
        return Optional.empty();
    }

    @Override
    protected Optional<Completable> createInstanceEmittingAMultipleValuesAndFailure(String v1, String v2,
                                                                               RuntimeException e) {
        return Optional.empty();
    }

    @Override
    protected Optional<Completable> createInstanceCompletingImmediately() {
        return empty();
    }

    @Override
    protected Optional<Completable> createInstanceCompletingAsynchronously() {
        return Optional.of(Completable
                .complete()
                .delay(10, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.computation())
                .andThen(Completable.complete())
        );
    }

    @Override
    protected Optional<Completable> never() {
        return Optional.of(Observable.never().ignoreElements());
    }

    @Override
    protected Optional<Completable> empty() {
        return Optional.of(Completable.complete());
    }

    @Override
    protected ReactiveTypeConverter<Completable> converter() {
        return converter;
    }

    @Override
    protected boolean supportNullValues() {
        return false;
    }
}
