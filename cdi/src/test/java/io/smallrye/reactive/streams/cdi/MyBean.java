package io.smallrye.reactive.streams.cdi;

import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.spi.ReactiveStreamsEngine;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class MyBean {

    @Inject
    private ReactiveStreamsEngine engine;

    public Integer sum() throws ExecutionException, InterruptedException {
        return ReactiveStreams.of(1, 2, 3).collect(Collectors.summingInt(i -> i)).run(engine).toCompletableFuture().get();
    }

}
