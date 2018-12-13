package io.smallrye.reactive.operators.snippets;

import org.eclipse.microprofile.reactive.streams.operators.ProcessorBuilder;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;
import org.reactivestreams.Publisher;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Operators {

    public <T> void empty() {
        // tag::empty[]
        PublisherBuilder<T> empty = ReactiveStreams.empty();
        // end::empty[]
    }

    public <T> void failed() {
        // tag::failed[]
        PublisherBuilder<T> failed = ReactiveStreams.failed(new Exception("BOOM!"));
        // end::failed[]
    }

    public <T> void fromCompletionStage(CompletionStage<T> cs) {
        // tag::fromCompletionStage[]
        PublisherBuilder<T> streamOfOne = ReactiveStreams.fromCompletionStage(cs);
        // If the redeemed value is `null`, an error is propagated in the stream.
        // end::fromCompletionStage[]
    }

    public <T> void fromCompletionStageNullable(CompletionStage<T> cs) {
        // tag::fromCompletionStageNullable[]
        PublisherBuilder<T> streamOfOne = ReactiveStreams.fromCompletionStageNullable(cs);
        // If the redeemed value is `null`, the stream is completed immediately.
        // end::fromCompletionStageNullable[]
    }

    public <T> void ofNullable(T maybeNull) {
        // tag::ofNullable[]
        PublisherBuilder<T> streamOfOneOrEmpty = ReactiveStreams.ofNullable(maybeNull);
        // end::ofNullable[]
    }

    public <T> void of(T t1, T t2, T t3) {
        // tag::of[]
        PublisherBuilder<T> streamOfOne = ReactiveStreams.of(t1);
        PublisherBuilder<T> streamOfThree = ReactiveStreams.of(t1, t2, t3);
        // end::of[]
    }

    public <T> void fromIterable(Iterable<T> iterable) {
        // tag::fromIterable[]
        PublisherBuilder<T> stream = ReactiveStreams.fromIterable(iterable);
        // If the iterable does not contain elements, the resulting stream is empty.
        // end::fromIterable[]
    }

    public <T> void fromPublisher(Publisher<T> publisher) {
        // tag::fromPublisher[]
        PublisherBuilder<T> stream = ReactiveStreams.fromPublisher(publisher);
        // If the publisher does not emit elements, the resulting stream is empty.
        // end::fromPublisher[]
    }

    public void generate() {
        // tag::generate[]
        AtomicInteger counter = new AtomicInteger();
        PublisherBuilder<Integer> stream = ReactiveStreams
                .generate(() -> counter.getAndIncrement());
        // The resulting stream is an infinite stream.
        // end::generate[]
    }

    public void iterate() {
        // tag::iterate[]
        PublisherBuilder<Integer> stream = ReactiveStreams
                .iterate(0, last -> last + 1);
        // The resulting stream is an infinite stream.
        // end::iterate[]
    }

    public <I, O> void builder() {
        // tag::builder[]
        ProcessorBuilder<I, O> builder = ReactiveStreams.<I>builder()
                .map(i -> (O) i); // Emit element of type O
        // end::builder[]
    }

    public void filtering() {
        // tag::filtering[]
        ReactiveStreams.of(1, 2, 3)
                .filter(i -> i > 2); // (1, 2)

        ReactiveStreams.of(2, 2, 3, 3, 2, 1)
                .distinct(); // (2, 3, 1)

        ReactiveStreams.of(2, 2, 3, 3, 2, 1)
                .dropWhile(i -> i == 2); // (3, 3, 2, 1)

        ReactiveStreams.of(2, 2, 3, 3, 2, 1)
                .skip(3); // (3, 2, 1)

        ReactiveStreams.of(2, 2, 3, 3, 2, 1)
                .limit(3); // (2, 2, 3)

        ReactiveStreams.of(2, 2, 3, 3, 2, 1)
                .takeWhile(i -> i == 2); // (2, 2)
        // end::filtering[]
    }

    public void composition() {
        // tag::composition[]
        ReactiveStreams.of(1, 2)
                .flatMap(i -> ReactiveStreams.of(i, i)); // (1, 1, 2, 2)

        ReactiveStreams.of(1, 2)
                .flatMapIterable(i -> Arrays.asList(i, i)); // (1, 1, 2, 2)

        ReactiveStreams.of(1, 2)
                .flatMap(i -> ReactiveStreams.of(i, i)); // (1, 1, 2, 2)

        ReactiveStreams.of(1, 2)
                .flatMapCompletionStage(i -> invokeAsyncService(i));
        // end::composition[]
    }

    public void map() {
        // tag::map[]
        ReactiveStreams.of(1, 2, 3)
                .map(i -> i + 1); // (2, 3, 4)
        // end::map[]
    }

    public void via() {
        //tag::via[]
        ProcessorBuilder<Integer, String> processor = ReactiveStreams
                .<Integer>builder().map(i -> Integer.toString(i));

        ReactiveStreams.of(1, 2)
                .via(processor); // ("1", "2")
        //end::via[]
    }

    private CompletionStage<Integer> invokeAsyncService(int i) {
        return CompletableFuture.completedFuture(i + 1);
    }

    public void ignore() {
        //tag::ignore[]
        ReactiveStreams.of(1, 2, 3)
                .peek(i -> System.out.println("Receiving: " + i))
                .ignore()
                .run()
                .thenAccept(x -> System.out.println("Done!"));
        //end::ignore[]
    }

    public void collect() {
        //tag::collect[]
        ReactiveStreams.of(1, 2, 3)
                .collect(Collectors.summingInt(i -> i))
                .run()
                // Produce 6
                .thenAccept(res -> System.out.println("Result is: " + res));

        ReactiveStreams.of(1, 2, 3)
                .collect(() -> new AtomicInteger(1), AtomicInteger::addAndGet)
                .run()
                // Produce 7
                .thenAccept(res -> System.out.println("Result is: " + res));

        ReactiveStreams.of(1, 2, 3)
                .reduce((acc, item) -> acc + item)
                .run()
                // Produce Optional<6>
                .thenAccept(res -> res.ifPresent(sum ->
                        System.out.println("Result is: " + sum)));

        ReactiveStreams.of(1, 2, 3)
                .toList()
                .run()
                // Produce [1, 2, 3]
                .thenAccept(res -> System.out.println("Result is: " + res));
        //end::collect[]
    }

    public void findFirst() {
        //tag::findFirst[]
        ReactiveStreams.of(1, 2, 3)
                .findFirst()
                .run()
                // Produce Optional[1]
                .thenAccept(maybe -> System.out.println(maybe));
        //end::findFirst[]
    }

    public void forEach() {
        //tag::forEach[]
        ReactiveStreams.of(1, 2, 3)
                .forEach(
                        i -> System.out.println("Receiving " + i)
                )
                .run();
        //end::forEach[]
    }

    public void to() {
        //tag::to[]
        SubscriberBuilder<Integer, Optional<Integer>> subscriber
                = ReactiveStreams.<Integer>builder()
                    .map(i -> i + 1)
                    .findFirst();

        ReactiveStreams.of(1, 2, 3)
                .to(subscriber)
                .run()
                // Produce Optional[2]
                .thenAccept(optional ->
                        optional.ifPresent(i -> System.out.println("Result: " + i)));
        //end::to[]
    }

}
