package io.smallrye.reactive.streams.stages;

import io.reactivex.subscribers.TestSubscriber;
import io.smallrye.reactive.streams.operators.PublisherStage;
import org.junit.Test;


/**
 * Checks the behavior of {@link FailedPublisherStageFactory}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FailedPublisherStageFactoryTest extends StageTestBase {

    private final FailedPublisherStageFactory factory = new FailedPublisherStageFactory();

    @Test
    public void createWithError() {
        Exception failure = new Exception("Boom");
        PublisherStage<Object> boom = factory.create(null, () -> failure);
        TestSubscriber<Object> test = boom.create().test();
        test.assertError(failure);
    }

    @Test(expected = NullPointerException.class)
    public void createWithoutError() {
        factory.create(null, () -> null);
    }

    @Test(expected = NullPointerException.class)
    public void createWithoutStage() {
        factory.create(null, null);
    }
}