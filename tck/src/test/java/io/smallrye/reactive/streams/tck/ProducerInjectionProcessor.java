package io.smallrye.reactive.streams.tck;

import io.smallrye.reactive.streams.cdi.ReactiveEngineProvider;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Just adding the {@link ReactiveEngineProvider} into the application deployment.
 */
public class ProducerInjectionProcessor implements ApplicationArchiveProcessor {
    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        applicationArchive.as(JavaArchive.class)
            .addClass(ReactiveEngineProvider.class);
    }
}
