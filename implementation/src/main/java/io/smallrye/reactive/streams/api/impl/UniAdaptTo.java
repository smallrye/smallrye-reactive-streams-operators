package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import io.smallrye.reactive.streams.api.adapter.UniAdapter;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;

public class UniAdaptTo<O> {

    private final Class<O> output;
    private final Uni<?> source;
    private final ServiceLoader<UniAdapter> adapters;

    public UniAdaptTo(Uni<?> uni, Class<O> output) {
        this.source = Objects.requireNonNull(uni, "The source cannot be `null`");
        this.output = Objects.requireNonNull(output, "The output cannot be `null`");
        this.adapters = ServiceLoader.load(UniAdapter.class);
    }

    @SuppressWarnings("unchecked")
    public O adapt() {

        if (output.isInstance(source)) {
            return (O) source;
        }

        if (output.isAssignableFrom(PublisherBuilder.class)) {
            return (O) ReactiveStreams.fromPublisher(source.toPublisher());
        }

        if (output.isAssignableFrom(CompletableFuture.class)) {
            return (O) source.subscribe().asCompletionStage();
        }

        for (UniAdapter adapter : this.adapters) {
            if (adapter.accept(output)) {
                return ((UniAdapter<O>) adapter).adaptTo(source);
            }
        }

        O instance = instantiateUsingFromPublisher();
        if (instance == null) {
            instance = instantiateUsingFrom();
            if (instance == null) {
                throw new RuntimeException("Unable to create an instance of " + output.getName() + " from a Uni, no adapter found");
            }
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private O instantiateUsingFromPublisher() {
        try {
            Method method = output.getMethod("fromPublisher", Publisher.class);
            if (Modifier.isStatic(method.getModifiers())) {
                return (O) method.invoke(null, source.toPublisher());
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private O instantiateUsingFrom() {
        try {
            Method method = output.getMethod("from", Publisher.class);
            if (Modifier.isStatic(method.getModifiers())) {
                return (O) method.invoke(null, source.toPublisher());
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
        return null;
    }

}
