package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class UniRecoveryTest {

    private Uni<Integer> failed = Uni.from().failure(IOException::new);


    @Test
    public void testRecoverWithDirectValue() {
        Integer result  = failed.recover().withResult(23).await().indefinitely();
        Integer result2  = Uni.of(1).recover().withResult(23).await().indefinitely();
        assertThat(result).isEqualTo(23);
        assertThat(result2).isEqualTo(1);
    }

    @Test
    public void testRecoverWithNullValue() {
        Integer result  = failed.recover().withResult((Integer) null).await().indefinitely();
        assertThat(result).isEqualTo(null);
    }

    @Test
    public void testRecoverWithSupplierOfValue() {
        AtomicInteger count = new AtomicInteger();
        Uni<Integer> recovered = failed.recover().withResult(() -> 23 + count.getAndIncrement());
        Integer result  = recovered.await().indefinitely();
        Integer result2  = recovered.await().indefinitely();
        assertThat(result).isEqualTo(23);
        assertThat(result2).isEqualTo(24);
    }

    @Test
    public void testWhenSupplierThrowsAnException() {
        Uni<Integer> recovered = failed.recover().withResult(() -> {
            throw new IllegalStateException("boom");
        });

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> recovered.await().indefinitely())
                .withMessage("boom");

    }

    @Test
    public void testRecoverWithFunctionProducingOfValue() {
        AtomicInteger count = new AtomicInteger();
        Uni<Integer> recovered = failed.recover().withResult(fail -> 23 + count.getAndIncrement());
        Integer result  = recovered.await().indefinitely();
        Integer result2  = recovered.await().indefinitely();
        assertThat(result).isEqualTo(23);
        assertThat(result2).isEqualTo(24);
    }

    @Test
    public void testWithPredicateOnClass() {
        Integer result  = failed.recover().fromFailure(IOException.class).withResult(23).await().indefinitely();
        assertThat(result).isEqualTo(23);
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> failed.recover().fromFailure(IllegalStateException.class).withResult(23).await().indefinitely())
                .withCauseExactlyInstanceOf(IOException.class);
    }

    @Test
    public void testWithPredicate() {
        Integer result  = failed.recover().fromFailure(f -> f instanceof IOException).withResult(23).await().indefinitely();
        assertThat(result).isEqualTo(23);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> failed.recover().fromFailure(f -> {
                    throw new IllegalArgumentException("BOOM!");
                }).withResult(23).await().indefinitely())
                .withMessageEndingWith("BOOM!");
    }

    @Test
    public void testRecoverWithUni() {
        Integer result  = failed.recover().withUni(Uni.of(25)).await().indefinitely();
        Integer result2  = Uni.of(1).recover().withUni(Uni.of(25)).await().indefinitely();
        assertThat(result).isEqualTo(25);
        assertThat(result2).isEqualTo(1);
    }

    @Test
    public void testRecoverWithUniNull() {
        Integer result  = failed.recover().withUni(Uni.of(1).map(i -> null)).await().indefinitely();
        assertThat(result).isEqualTo(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRecoverWithUniFail() {
        failed.recover().withUni(Uni.from().failure(IllegalArgumentException::new)).await().indefinitely();
    }

    @Test
    public void testRecoverWithSupplierOfUni() {
        AtomicInteger count = new AtomicInteger();
        Uni<Integer> uni = failed.recover().withUni(() -> Uni.from().value(() -> 25 + count.incrementAndGet()));
        Integer result  = uni.await().indefinitely();
        Integer result2  = uni.await().indefinitely();
        assertThat(result).isEqualTo(26);
        assertThat(result2).isEqualTo(27);
    }

    @Test
    public void testWhenSupplierOfUniThrowsAnException() {
        Uni<Integer> recovered = failed.recover().withUni(() -> {
            throw new IllegalStateException("boom");
        });

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> recovered.await().indefinitely())
                .withMessage("boom");

    }

    @Test
    public void testRecoverWithFunctionProducingOfUni() {
        AtomicInteger count = new AtomicInteger();
        Uni<Integer> recovered = failed.recover().withUni(fail -> Uni.from().value(() -> 23 + count.getAndIncrement()));
        Integer result  = recovered.await().indefinitely();
        Integer result2  = recovered.await().indefinitely();
        assertThat(result).isEqualTo(23);
        assertThat(result2).isEqualTo(24);
    }

    @Test
    public void testRecoveringWithUniWithPredicateOnClass() {
        Integer result  = failed.recover().fromFailure(IOException.class).withUni(Uni.of(23)).await().indefinitely();
        assertThat(result).isEqualTo(23);
        assertThatExceptionOfType(CompletionException.class)
                .isThrownBy(() -> failed.recover().fromFailure(IllegalStateException.class).withUni(Uni.of(23)).await().indefinitely())
                .withCauseExactlyInstanceOf(IOException.class);
    }

    @Test
    public void testRecoveringWithUniWithPredicate() {
        Integer result  = failed.recover().fromFailure(f -> f instanceof IOException).withUni(Uni.of(23)).await().indefinitely();
        assertThat(result).isEqualTo(23);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> failed.recover().fromFailure(f -> {
                    throw new IllegalArgumentException("BOOM!");
                }).withResult(23).await().indefinitely())
                .withMessageEndingWith("BOOM!");
    }


    @Test
    public void testNotCalledOnResult() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        Uni.of(1)
                .recover().withUni(v -> Uni.of(2))
                .subscribe().withSubscriber(ts);
        ts.assertCompletedSuccessfully().assertResult(1);
    }


    @Test
    public void testCalledOnFailure() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();

        Uni.from().<Integer>failure(new RuntimeException("boom"))
                .recover().withUni(fail -> Uni.of(2))
                .subscribe().withSubscriber(ts);

        ts.assertCompletedSuccessfully().assertResult(2);
    }

    @Test
    public void testCalledOnFailureWithDirectResult() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();

        Uni.from().<Integer>failure(new RuntimeException("boom"))
                .recover().withResult(fail -> 2)
                .subscribe().withSubscriber(ts);

        ts.assertCompletedSuccessfully().assertResult(2);
    }

    @Test
    public void testWithMappingOfFailure() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        Uni.from().<Integer>failure(new Exception())
                .map().failure(f -> new RuntimeException("boom"))
                .subscribe().withSubscriber(ts);
        ts.assertCompletedWithFailure()
                .assertFailure(RuntimeException.class, "boom");
    }


    @Test
    public void testWithMappingOfFailureAndPredicates() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        Uni.from().<Integer>failure(new IOException())
                .map().failure(t -> new IndexOutOfBoundsException())
                .recover().fromFailure(IOException.class).withUni(Uni.of(1))
                .recover().fromFailure(IndexOutOfBoundsException.class).withUni(Uni.of(2))
                .subscribe().withSubscriber(ts);
        ts.assertCompletedSuccessfully().assertResult(2);
    }


}
