package io.smallrye.reactive.streams.cdi;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.reactive.streams.operators.spi.ReactiveStreamsEngine;

public class ReactiveEngineProvider {

    /**
     * @return the reactive stream engine. It uses {@link ServiceLoader#load(Class)} to find an implementation from the
     *         Classpath.
     * @throws IllegalStateException if no implementations are found.
     */
    @Produces
    @ApplicationScoped
    public ReactiveStreamsEngine getEngine() {
        Iterator<ReactiveStreamsEngine> iterator = ServiceLoader.load(ReactiveStreamsEngine.class).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        throw new IllegalStateException("No implementation of the "
                + ReactiveStreamsEngine.class.getName() + " found in the Classpath");
    }

}
