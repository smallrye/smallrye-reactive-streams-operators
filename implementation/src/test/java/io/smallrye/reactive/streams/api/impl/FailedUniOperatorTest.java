package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class FailedUniOperatorTest {


    @Test
    public void testCreationWithCheckedException() {
        AssertSubscriber<Object> ts = AssertSubscriber.create();
        Uni.failed(new Exception("boom")).subscribe(ts);
        ts.assertFailed(Exception.class, "boom");

        try {
            Uni.failed(new Exception("boom")).block();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e).hasCauseInstanceOf(Exception.class)
                    .isInstanceOf(RuntimeException.class);
        }

    }

    @Test
    public void testCreationWithRuntimeException() {
        AssertSubscriber<Object> ts = AssertSubscriber.create();
        Uni.failed(new RuntimeException("boom")).subscribe(ts);
        ts.assertFailed(RuntimeException.class, "boom");

        try {
            Uni.failed(new RuntimeException("boom")).block();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("boom");
        }
    }

    @Test(expected = NullPointerException.class)
    public void testCreationWithNull() {
        Uni.failed(null);
    }

}