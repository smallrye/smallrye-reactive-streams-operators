package io.smallrye.reactive.operators.quickstart;

import org.apache.camel.CamelContext;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreams;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.apache.camel.impl.DefaultCamelContext;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.reactivestreams.Subscriber;

import java.io.File;
import java.nio.file.Files;

public class QuickStart {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        CamelReactiveStreamsService camel = CamelReactiveStreams.get(context);
        Subscriber<String> subscriber = camel
                .subscriber("file:./target?fileName=values.txt&fileExist=append", String.class);

        ReactiveStreams.of("hello", "from", "smallrye", "reactive", "stream", "operators",
                "using", "Apache", "Camel")
                .map(String::toUpperCase) // Transform the words
                .filter(s -> s.length() > 4) // Filter items
                .peek(System.out::println)
                .map(s -> s + " ")
                .to(subscriber)
                .run();
        context.start();

        // Just wait until it's done.
        Thread.sleep(1000);

        Files.readAllLines(new File("target/values.txt").toPath())
                .forEach(s -> System.out.println("File >> " + s));
    }

}
