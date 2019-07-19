package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniEmitter;
import io.smallrye.reactive.streams.api.groups.UniFromGroup;
import org.reactivestreams.Publisher;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class UniFromGroupImpl implements UniFromGroup {

    public static UniFromGroup INSTANCE = new UniFromGroupImpl();

    private UniFromGroupImpl() {
        // Avoid direct instantiation.
    }

    @Override
    public <T> Uni<T> completionStage(CompletionStage<? extends T> stage) {
        nonNull(stage, "stage");
        return completionStage(() -> stage);
    }

    @Override
    public <T> Uni<T> completionStage(Supplier<? extends CompletionStage<? extends T>> supplier) {
        return new UniFromCompletionStageSupplier<>(
                nonNull(supplier, "supplier"));
    }

    @Override
    public <T> Uni<T> publisher(Publisher<? extends T> publisher) {
        return new UniFromPublisher<>(nonNull(publisher, "publisher"));
    }

    @Override
    public <T> Uni<T> value(Supplier<? extends T> supplier) {
        nonNull(supplier, "supplier");
        return emitter(emitter -> {
            T result;
            try {
                result = supplier.get();
            } catch (RuntimeException e) {
                // Exception from the supplier, propagate it.
                emitter.fail(e);
                return;
            }
            emitter.success(result);
        });
    }

    @Override
    public <T> Uni<T> value(T value) {
        return value(() -> value);
    }

    @Override
    public <T> Uni<T> optional(Optional<T> optional) {
        return value(nonNull(optional, "optional").orElse(null));
    }

    @SuppressWarnings("OptionalAssignedToNull")
    @Override
    public <T> Uni<T> optional(Supplier<Optional<T>> supplier) {
        nonNull(supplier, "supplier");
        return value(() -> {
            Optional<T> optional = supplier.get();
            if (optional == null) {
                throw new NullPointerException("The `supplier` produced a `null` value");
            }
            return optional.orElse(null);
        });
    }

    @Override
    public Uni<Void> delay(Duration duration, ScheduledExecutorService executor) {
        nonNull(duration, "duration");
        nonNull(executor, "executor");
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("`duration` must be greater than 0");
        }
        return emitter(emitter ->
                executor.schedule(() -> emitter.success(null), duration.toMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public <T> Uni<T> emitter(Consumer<UniEmitter<? super T>> consumer) {
        return new UniCreate<>(nonNull(consumer, "consumer"));
    }

    @Override
    public <T> Uni<T> deferred(Supplier<? extends Uni<? extends T>> supplier) {
        return new UniDefer<>(nonNull(supplier, "supplier"));
    }

    @Override
    public <T> Uni<T> failure(Throwable failure) {
        nonNull(failure, "failure");
        return failure(() -> failure);
    }

    @Override
    public <T> Uni<T> failure(Supplier<Throwable> supplier) {
        nonNull(supplier, "supplier");
        return emitter(emitter -> {
            Throwable throwable;
            try {
                throwable = supplier.get();
            } catch (RuntimeException e) {
                // Exception from the supplier, propagate it.
                emitter.fail(e);
                return;
            }

            if (throwable == null) {
                // The supplier produced `null`, propagate a NullPointerException
                emitter.fail(new NullPointerException("`supplier` produced a `null` value"));
            } else {
                emitter.fail(throwable);
            }
        });
    }

    @Override
    public <T> Uni<T> nothing() {
        return completionStage(new CompletableFuture<>());
    }

    @Override
    public Uni<Void> nullValue() {
        return value((Void) null);
    }
}
