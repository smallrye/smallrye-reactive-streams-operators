package io.smallrye.reactive.streams.tck;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * Arquillian extension to add the CDI provider into the application.
 */
public class ApplicationEnhancer implements LoadableExtension {
    @Override
    public void register(LoadableExtension.ExtensionBuilder extensionBuilder) {
        extensionBuilder
            .service(ApplicationArchiveProcessor.class, ProducerInjectionProcessor.class);
    }
}
