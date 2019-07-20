package io.smallrye.reactive.streams.api.groups;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.impl.UniOr;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import static io.smallrye.reactive.streams.api.impl.ParameterValidation.nonNull;

public class UniAnyGroup {

    public final static UniAnyGroup INSTANCE = new UniAnyGroup();

    private UniAnyGroup() {
        // avoid direct instantiation.
    }


    /**
     * Like {@link #of(Iterable)} but with an array of {@link Uni} as parameter
     *
     * @param unis the array, must not be {@code null}, must not contain @{code null}
     * @param <T> the type of result emitted by the different unis.
     * @return the produced {@link Uni}
     */
    @SafeVarargs
    public final <T> Uni<T> of(Uni<? super T>... unis) {
        List<Uni<? super T>> list = Arrays.asList(nonNull(unis, "unis"));
        return new UniOr<>(list);
    }

    /**
     * Creates a {@link Uni} forwarding the first signal (value, {@code null} or failure). It behaves like the fastest
     * of these competing unis. If the passed iterable is empty, the resulting {@link Uni} gets a {@code null} result
     * just after subscription.
     * <p>
     * This method subscribes to the set of {@link Uni}. When one of the {@link Uni} resolves successfully or with
     * a failure, the signals is propagated to the returned {@link Uni}. Also the other subscriptions are cancelled.
     * Note that the callback from the subscriber are called on the thread used to resolve the winning {@link Uni}.
     * Use {@link Uni#publishOn(Executor)} to change the thread.
     * <p>
     * If the subscription to the returned {@link Uni} is cancelled, the subscription to the {@link Uni unis} from the
     * {@code iterable} are also cancelled.
     *
     * @param iterable a set of {@link Uni}, must not be {@code null}.
     * @param <T> the type of result emitted by the different unis.
     * @return the produced {@link Uni}
     */
    public <T> Uni<T> of(Iterable<? extends Uni<? super T>> iterable) {
        return new UniOr<>(iterable);
    }
}
