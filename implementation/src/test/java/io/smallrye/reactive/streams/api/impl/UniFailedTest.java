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
        Uni<Object> failed = Uni.failed(() -> new IOException("boom"));
        try {
            failed.block();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e).hasCauseInstanceOf(IOException.class);
        }
    }

    @Test
    public void testCreationWithCheckedException() {
        AssertSubscriber<Object> ts = AssertSubscriber.create();
        Uni.failed(new Exception("boom")).subscribe(ts);
        ts.assertFailure(Exception.class, "boom");

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
        ts.assertFailure(RuntimeException.class, "boom");

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
        Uni.failed((Exception) null);
    }

    @Test(expected = NullPointerException.class)
    public void testCreationWithNullAsSupplier() {
        Uni.failed((Supplier<? extends Throwable>) null);
    }

    @Test
    public void testWithASupplierReturningNull() {
        Uni<Object> failed = Uni.failed(() -> null);
        try {
            failed.block();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    public void testWithASupplierThrowingAnException() {
        Uni<Object> failed = Uni.failed(() -> {
            throw new NoSuchElementException("boom");
        });
        try {
            failed.block();
            fail("Exception expected");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(NoSuchElementException.class);
        }
    }


}