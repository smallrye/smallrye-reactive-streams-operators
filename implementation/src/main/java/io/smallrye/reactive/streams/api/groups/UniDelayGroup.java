package io.smallrye.reactive.streams.api.groups;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.impl.Infrastructure;
import io.smallrye.reactive.streams.api.impl.UniDelay;
import io.smallrye.reactive.streams.api.impl.UniDelayUntil;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

public class UniDelayGroup<T> {

    private final Uni<T> source;
    private final ScheduledExecutorService executor;

    public UniDelayGroup(Uni<T> source, ScheduledExecutorService executor) {
        this.source = source;
        this.executor = executor == null ? Infrastructure.getDefaultExecutor() : executor;
    }

    public UniDelayGroup<T> onExecutor(ScheduledExecutorService executor) {
        return new UniDelayGroup<>(source, executor);
    }

    public Uni<T> of(Duration duration) {
        return new UniDelay<>(source, duration, executor);
    }

    // TODO Add result and subscription

    public Uni<T> until(Function<? super T, ? extends Uni<?>> function) {
        return new UniDelayUntil(source, function, executor);
    }

}
