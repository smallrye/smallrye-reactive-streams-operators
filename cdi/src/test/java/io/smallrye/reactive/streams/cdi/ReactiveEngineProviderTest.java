package io.smallrye.reactive.streams.cdi;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check that beans can access the engine.
 */
public class ReactiveEngineProviderTest {


    private Weld weld;
    private WeldContainer container;

    @Before
    public void setUp() {
        weld = new Weld();
        weld.addBeanClasses(MyBean.class);
    }

    @Test
    public void testThatABeanCanAccessTheEngine() throws ExecutionException, InterruptedException {
        container = weld.initialize();
        MyBean myBean = container.getBeanManager().createInstance().select(MyBean.class).get();
        int result = myBean.sum();
        assertThat(result).isEqualTo(1 + 2 + 3);
    }

    @After
    public void tearDown() {
        if (container != null) {
            container.close();
        }
    }

}