package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class UniMapToFailureTest {

    private Uni<Integer> one = Uni.of(1);

    @Test(expected = IllegalArgumentException.class)
    public void testThatMapperCannotBeNull() {
        one = Uni.of(1);
        one.map().toFailure(null);
    }

    @Test
    public void testMapToException() {
        AtomicInteger count = new AtomicInteger();
        Uni<Integer> uni = one.map().toFailure(s -> new IOException(Integer.toString(s + count.getAndIncrement())));
        uni
                .subscribe().withSubscriber(AssertSubscriber.<Number>create())
                .assertFailure(IOException.class, "1");
        uni
                .subscribe().withSubscriber(AssertSubscriber.<Number>create())
                .assertFailure(IOException.class, "2");
    }

}
