package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

public class UniNeverTest {

    @Test
    public void testTheBehaviorOfNever() {
        AssertSubscriber<Void> subscriber = AssertSubscriber.create();
        Uni.from().<Void>nothing()
                .subscribe().withSubscriber(subscriber);
        subscriber.assertNoResult().assertNoResult().assertSubscribed();
    }
}
