package io.smallrye.reactive.converters;

import java.util.concurrent.CompletionStage;

public interface FutureConverter<T> {

    CompletionStage toCompletionStage(T instance);

    T fromCompletionStage(CompletionStage cs);

    Class<T> type();

}
