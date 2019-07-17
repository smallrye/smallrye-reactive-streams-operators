package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.UniEmitter;
import io.smallrye.reactive.streams.api.UniFromGroup;
import org.reactivestreams.Publisher;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UniFromGroupImpl implements UniFromGroup {

    public static UniFromGroup INSTANCE = new UniFromGroupImpl();

    private UniFromGroupImpl() {
        // Avoid direct instantiation.
    }

    @Override
    public <T> Uni<T> completionStage(CompletionStage<? extends T> stage) {
        Objects.requireNonNull(stage, "`stage` must not be `null`");
        return completionStage(() -> stage);
    }

    @Override
    public <T> Uni<T> completionStage(Supplier<? extends CompletionStage<? extends T>> supplier) {
        return new UniFromCompletionStageSupplier<>(
                Objects.requireNonNull(supplier, "`supplier` must not be `null`"));
    }

    @Override
    public <T> Uni<T> publisher(Publisher<? extends T> publisher) {
        return new UniFromPublisher<>(Objects.requireNonNull(publisher, "`publisher` must not be `null`"));
    }

    @Override
    public <T> Uni<T> value(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "`supplier` must not be `null`");
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
        return value(Objects.requireNonNull(optional, "`optional` must not be `null`").orElse(null));
    }

    @SuppressWarnings("OptionalAssignedToNull")
    @Override
    public <T> Uni<T> optional(Supplier<Optional<T>> supplier) {
        Objects.requireNonNull(supplier, "`supplier` must not be `null`");
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
        Objects.requireNonNull(duration, "`duration` must not be `null`");
        Objects.requireNonNull(executor, "`executor` must not be `null`");
        if (duration.isNegative()  || duration.isZero()) {
            throw new IllegalArgumentException("`duration` must be greater than 0");
        }
        return emitter(emitter ->
                executor.schedule(() -> emitter.success(null),  duration.toMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public <T> Uni<T> emitter(Consumer<UniEmitter<? super T>> consumer) {
        return new UniCreate<>(Objects.requireNonNull(consumer, "`consumer` must not be `null`"));
    }

    @Override
    public <T> Uni<T> deferredUni(Supplier<? extends Uni<? extends T>> supplier) {
        return new UniDefer<>(Objects.requireNonNull(supplier, "`supplier` must not be `null`"));
    }

    @Override
    public <T> Uni<T> failure(Throwable failure) {
        Objects.requireNonNull(failure, "`failure` must not be `null`");
        return failure(() -> failure);
    }

    @Override
    public <T> Uni<T> failure(Supplier<Throwable> supplier) {
        Objects.requireNonNull(supplier, "`supplier` must not be `null`");
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
