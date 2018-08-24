package io.smallrye.reactive.streams.cdi;

import io.smallrye.reactive.streams.Engine;
import org.eclipse.microprofile.reactive.streams.spi.ReactiveStreamsEngine;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class ReactiveEngineProvider {

    @Produces
    public ReactiveStreamsEngine getEngine() {
        return new Engine();
    }

}
