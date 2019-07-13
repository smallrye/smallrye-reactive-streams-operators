package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class UniDeferTest {

    @Test
    public void test() {
        AtomicInteger counter = new AtomicInteger();
        Uni<Integer> s = Uni.defer(() -> Uni.of(counter.incrementAndGet()));

        for (int i = 1; i < 100; i++) {
            assertThat(s.block()).isEqualTo(i);
        }
    }
}
