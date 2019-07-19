package io.smallrye.reactive.streams.api.groups;

import io.smallrye.reactive.streams.api.TimeoutException;
import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.impl.UniFailOnTimeout;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;
import static io.smallrye.reactive.streams.api.impl.ParameterValidation.validate;

public class UniOnTimeoutGroup<T> {

    private final Uni<T> source;
    private final Duration timeout;
    private final ScheduledExecutorService executor;

    public UniOnTimeoutGroup(Uni<T> source, Duration timeout, ScheduledExecutorService executor) {
        this.source = nonNull(source, "source");
        this.timeout = timeout;
        this.executor = executor;
    }

    /**
     * Configures the timeout duration.
     *
     * @param timeout the timeout, must not be {@code null}, must be strictly positive.
     * @return a new {@link UniOnTimeoutGroup}
     */
    public UniOnTimeoutGroup<T> of(Duration timeout) {
        return new UniOnTimeoutGroup<>(source, validate(timeout, "timeout"), executor);
    }

    public UniOnTimeoutGroup<T> on(ScheduledExecutorService executor) {
        return new UniOnTimeoutGroup<>(source, timeout, executor);
    }

    public UniOnTimeoutRecoveryGroup<T> recover() {
        validate(timeout, "timeout");
        return new UniOnTimeoutRecoveryGroup<>(this);
    }

    public Uni<T> fail() {
        return failWith(TimeoutException::new);
    }

    public Uni<T> failWith(Throwable failure) {
        return failWith(() -> failure);
    }

    public Uni<T> failWith(Supplier<Throwable> supplier) {
        validate(timeout, "timeout");
        return new UniFailOnTimeout<>(source, timeout, supplier, null);
    }


}
