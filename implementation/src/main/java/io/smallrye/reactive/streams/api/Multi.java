/*******************************************************************************
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package io.smallrye.reactive.streams.api;

import org.eclipse.microprofile.reactive.streams.operators.CompletionRunner;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * An implementation for a {@link Publisher} providing a set of operators.
 * <p>
 * <Strong>INCOMPLETE!</Strong>
 *
 * @param <T> The type of the elements that the publisher emits.
 * @see ReactiveStreams
 */
public interface Multi<T> {


    <R> Multi<R> map(Function<? super T, ? extends R> mapper);


    <S> Multi<S> flatMap(Function<? super T, ? extends Multi<? extends S>> mapper);


    <S> Multi<S> flatMapCompletionStage(Function<? super T, ? extends CompletionStage<? extends S>> mapper);


    <S> Multi<S> flatMapUni(Function<? super T, ? extends CompletionStage<? extends S>> mapper);


    <S> Multi<S> flatMapIterable(Function<? super T, ? extends Iterable<? extends S>> mapper);


    Multi<T> filter(Predicate<? super T> predicate);


    Multi<T> distinct();


    Multi<T> limit(long maxSize);


    Multi<T> skip(long n);


    Multi<T> takeWhile(Predicate<? super T> predicate);


    // TODO Rename to skipUntil
    Multi<T> dropWhile(Predicate<? super T> predicate);


    // TODO Add onNext
    Multi<T> peek(Consumer<? super T> consumer);


    Multi<T> onError(Consumer<Throwable> errorHandler);


    Multi<T> onTerminate(Runnable action);


    Multi<T> onComplete(Runnable action);

    // TODO should it subscribe or be lazy. If lazy return Uni, if Subscribe return CompletionStage
    Uni<Void> forEach(Consumer<? super T> action);


    Uni<Void> ignore();

    // TODO should it subscribe or be lazy. If lazy return Uni, if Subscribe return CompletionStage
    CompletionRunner<Void> cancel();


    Uni<T> reduce(T identity, BinaryOperator<T> accumulator);

    // TODO Optional or `null`? and the accumulator can return null, we can't distinguish empty and `null` result
    Uni<Optional<T>> reduce(BinaryOperator<T> accumulator);

    // null on empty
    Uni<T> first();


    <R, A> Uni<R> collect(Collector<? super T, A, R> collector);

    <R> Uni<R> collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator);


    Uni<List<T>> toList();


    Multi<T> onErroReturn(Function<Throwable, ? extends T> errorHandler);


    Multi<T> onErrorResume(Function<Throwable, ? extends Multi<? extends T>> errorHandler);

    //TODO is is terminal and so must return a CompletionStage, or just return void?
    // If not terminal Uni
    CompletionStage<Void> to(Subscriber<? super T> subscriber);

    <R> Multi<R> via(Processor<? super T, ? extends R> processor);

}
