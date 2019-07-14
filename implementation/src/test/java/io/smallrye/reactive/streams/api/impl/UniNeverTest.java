package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

public class UniNeverTest {

    @Test
    public void testTheBehaviorOfNever() {
        AssertSubscriber<Void> subscriber = AssertSubscriber.create();
        Uni.<Void>never()
                .subscribe(subscriber);
        subscriber.hasNoValue().hasNoValue().hasSubscription();
    }
}
