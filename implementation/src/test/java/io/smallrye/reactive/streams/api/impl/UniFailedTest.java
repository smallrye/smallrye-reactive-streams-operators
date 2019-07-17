package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class UniFailedTest {

    @Test
    public void testWithASupplier() {
        Uni<Object> boom = Uni.from().failure(() -> new IOException("boom"));
        try {
            boom.await().indefinitely();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e).hasCauseInstanceOf(IOException.class);
        }
    }

    @Test
    public void testCreationWithCheckedException() {
        AssertSubscriber<Object> ts = AssertSubscriber.create();
        Uni.from().failure(new Exception("boom")).subscribe().withSubscriber(ts);
        ts.assertFailure(Exception.class, "boom");

        try {
            Uni.from().failure(new Exception("boom")).await().asOptional().indefinitely();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e).hasCauseInstanceOf(Exception.class)
                    .isInstanceOf(RuntimeException.class);
        }

    }

    @Test
    public void testCreationWithRuntimeException() {
        AssertSubscriber<Object> ts = AssertSubscriber.create();
        Uni.from().failure(new RuntimeException("boom")).subscribe().withSubscriber(ts);
        ts.assertFailure(RuntimeException.class, "boom");

        try {
            Uni.from().failure(new RuntimeException("boom")).await().indefinitely();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("boom");
        }
    }


    @Test(expected = NullPointerException.class)
    public void testCreationWithNull() {
        Uni.from().failure((Exception) null);
    }

    @Test(expected = NullPointerException.class)
    public void testCreationWithNullAsSupplier() {
        Uni.from().failure((Supplier<Throwable>) null);
    }

    @Test
    public void testWithASupplierReturningNull() {
        Uni<Object> boom = Uni.from().failure(() -> null);
        try {
            boom.await().indefinitely();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    public void testWithASupplierThrowingAnException() {
        Uni<Object> boom = Uni.from().failure(() -> {
            throw new NoSuchElementException("boom");
        });
        try {
            boom.await().indefinitely();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(NoSuchElementException.class);
        }
    }


}