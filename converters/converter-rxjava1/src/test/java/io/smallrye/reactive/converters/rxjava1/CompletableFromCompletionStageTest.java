package io.smallrye.reactive.converters.rxjava1;

import io.smallrye.reactive.converters.ReactiveTypeConverter;
import io.smallrye.reactive.converters.Registry;
import io.smallrye.reactive.converters.tck.FromCompletionStageTCK;
import org.junit.Before;
import org.junit.Test;
import rx.Completable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class CompletableFromCompletionStageTest extends FromCompletionStageTCK<Completable> {

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
    protected boolean emitValues() {
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


    @Test
    public void testWithImmediateCompletion() {
        AtomicBoolean reference = new AtomicBoolean();
        @SuppressWarnings("unchecked")
        Completable completable = converter
                .fromCompletionStage(CompletableFuture.runAsync(() -> {
                }));
        completable
                .doOnCompleted(() -> reference.set(true))
                .await();
        assertThat(reference).isTrue();
    }



    @Test
    public void testWithAsynchronousCompletion() {
        AtomicBoolean reference = new AtomicBoolean();
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        });
        @SuppressWarnings("unchecked")
        Completable completable = converter
                .fromCompletionStage(future);
        completable
                .doOnCompleted(() -> reference.set(true))
                .await();
        assertThat(reference).isTrue();
    }

}
