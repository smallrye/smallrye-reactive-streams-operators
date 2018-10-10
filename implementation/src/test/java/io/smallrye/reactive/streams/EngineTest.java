package io.smallrye.reactive.streams;

import io.reactivex.Flowable;
import org.eclipse.microprofile.reactive.streams.CompletionSubscriber;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.spi.Graph;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.eclipse.microprofile.reactive.streams.spi.UnsupportedStageException;
import org.junit.Test;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link Engine} class.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class EngineTest {

    private Engine engine;

    @Test
    public void create() {
        engine = new Engine();
        assertThat(engine).isNotNull();
    }

    @Test
    public void testValidSubscriber() {
        engine = new Engine();
        CompletionSubscriber<Integer, Optional<Integer>> built =
                ReactiveStreams.<Integer>builder()
                        .map(i -> i + 1)
                        .findFirst()
                        .build(engine);

        assertThat(built).isNotNull();

        ReactiveStreams.of(5, 4, 3).buildRs().subscribe(built);
        Optional<Integer> integer = built.getCompletion().toCompletableFuture().join();
        assertThat(integer).contains(6);
    }

    @Test(expected = UnsupportedStageException.class)
    public void testUnknownTerminalStage() {
        engine = new Engine();
        List<Stage> stages = new ArrayList<>();
        stages.add((Stage.Map) () -> i -> (int) i + 1);
        stages.add(new Stage() {
            // Unknown stage
        });
        Graph graph = () -> stages;
        engine.buildSubscriber(graph);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSubscriber() {
        engine = new Engine();
        List<Stage> stages = new ArrayList<>();
        stages.add((Stage.Map) () -> i -> (int) i + 1);
        // This graph is not closed - so it's invalid
        Graph graph = () -> stages;
        engine.buildSubscriber(graph);
    }

    @Test
    public void testValidCompletion() {
        engine = new Engine();
        List<Stage> stages = new ArrayList<>();
        stages.add((Stage.PublisherStage) Flowable::empty);
        stages.add((Stage.Map) () -> i -> (int) i + 1);
        stages.add(new Stage.FindFirst() {});
        Graph graph = () -> stages;
        assertThat(engine.buildCompletion(graph)).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCompletion() {
        engine = new Engine();
        List<Stage> stages = new ArrayList<>();
        stages.add((Stage.PublisherStage) Flowable::empty);
        stages.add((Stage.Map) () -> i -> (int) i + 1);
        // This graph is not closed - so it's invalid
        Graph graph = () -> stages;
        engine.buildCompletion(graph);
    }

    @Test(expected = UnsupportedStageException.class)
    public void testCompletionWithUnknownStage() {
        engine = new Engine();
        List<Stage> stages = new ArrayList<>();
        stages.add((Stage.PublisherStage) Flowable::empty);
        stages.add((Stage.Map) () -> i -> (int) i + 1);
        stages.add(new Stage() {
           // Unknown stage.
        });
        stages.add(new Stage.FindFirst() {});
        Graph graph = () -> stages;
        assertThat(engine.buildCompletion(graph)).isNotNull();
    }

    @Test
    public void testValidPublisher() {
        engine = new Engine();
        List<Stage> stages = new ArrayList<>();
        stages.add((Stage.Of) () -> Arrays.asList(1, 2, 3));
        stages.add((Stage.Map) () -> i -> (int) i + 1);
        Graph graph = () -> stages;
        assertThat(engine.buildPublisher(graph)).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPublisher() {
        engine = new Engine();
        List<Stage> stages = new ArrayList<>();
        stages.add((Stage.Map) () -> i -> (int) i + 1);
        stages.add(new Stage.FindFirst() {});
        // This graph is closed, invalid as publisher
        Graph graph = () -> stages;
        engine.buildPublisher(graph);
    }

    @Test(expected = UnsupportedStageException.class)
    public void testCreatingPublisherWithUnknownStage() {
        engine = new Engine();
        List<Stage> stages = new ArrayList<>();
        stages.add(new Stage() {
            // Unknown stage.
        });
        stages.add((Stage.Map) () -> i -> (int) i + 1);
        Graph graph = () -> stages;
        engine.buildPublisher(graph);
    }


}