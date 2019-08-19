package io.smallrye.reactive.streams.stages;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

/**
 * Creates and disposes the engine.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class StageTestBase {

    PublisherBuilder<Integer> infiniteStream() {
        return ReactiveStreams.fromIterable(() -> {
            AtomicInteger value = new AtomicInteger();
            return IntStream.generate(value::incrementAndGet).boxed().iterator();
        });
    }
}
