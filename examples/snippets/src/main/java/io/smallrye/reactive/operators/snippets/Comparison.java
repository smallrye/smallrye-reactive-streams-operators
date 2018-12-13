package io.smallrye.reactive.operators.snippets;

import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

import java.util.stream.Stream;

public class Comparison {

    public static void main(String[] args) {
        comparison();
    }

    private static void comparison() {
        // tag::snippet[]
        // java.util.stream version:
        Stream.of("hello", "world")
                .filter(word -> word.length() <= 5)
                .map(String::toUpperCase)
                .findFirst()
                .ifPresent(s -> System.out.println("Regular Java stream result: " + s));
        // reactive stream operator version:
        ReactiveStreams.of("hello", "world")
                .filter(word -> word.length() <= 5)
                .map(String::toUpperCase)
                .findFirst()
                .run() // Run the stream (start publishing)
                // Retrieve the result asynchronously, using a CompletionStage
                .thenAccept(res -> res
                        .ifPresent(s -> System.out.println("Reactive Stream result: " + s)));
        // end::snippet[]
    }

}
