package io.smallrye.reactive.streams;

import io.reactivex.Flowable;
import io.vertx.core.Future;
import io.vertx.core.impl.VertxInternal;
import io.vertx.reactivex.core.Vertx;
import org.eclipse.microprofile.reactive.streams.spi.Graph;
import org.eclipse.microprofile.reactive.streams.spi.Stage;
import org.eclipse.microprofile.reactive.streams.spi.UnsupportedStageException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the {@link Engine} class.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class EngineTest {

  private Engine engine;

  @BeforeClass
  public static void reset() {
    if (Engine.reset()) {
      System.out.println("Default Vert.x instance re-initialized");
    }
  }

  @After
  public void tearDown() {
    if (engine != null) {
      engine.close();
    }
  }

  @Test
  public void createWithoutVertxInstance() {
    engine = new Engine();
    assertThat(engine.vertx()).isNotNull().isInstanceOf(Vertx.class);
  }

  @Test
  public void createWithRXVertxInstance() {
    Vertx vertx = Vertx.vertx();
    engine = new Engine(vertx);
    assertThat(engine.vertx()).isNotNull().isInstanceOf(Vertx.class).isEqualTo(vertx);
    vertx.close();
  }

  @Test
  public void createWithNonRXVertxInstance() {
    io.vertx.core.Vertx vertx = io.vertx.core.Vertx.vertx();
    engine = new Engine(vertx);
    assertThat(engine.vertx()).isNotNull().isInstanceOf(Vertx.class);
    assertThat(engine.vertx().getDelegate()).isEqualTo(vertx);
    vertx.close();
  }

  @Test
  public void testValidSubscriber() {
    engine = new Engine();
    List<Stage> stages = new ArrayList<>();
    stages.add(new Stage.Map(i -> ((int) i) + 1));
    stages.add(Stage.FindFirst.INSTANCE);
    Graph graph = new Graph(stages);
    assertThat(engine.buildSubscriber(graph)).isNotNull();
  }

  @Test(expected = UnsupportedStageException.class)
  public void testUnknownTerminalStage() {
    engine = new Engine();
    List<Stage> stages = new ArrayList<>();
    stages.add(new Stage.Map(i -> ((int) i) + 1));
    stages.add(new Stage() {
      @Override
      public boolean hasInlet() {
        return true;
      }

      @Override
      public boolean hasOutlet() {
        return false;
      }
    });
    Graph graph = new Graph(stages);
    engine.buildSubscriber(graph);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidSubscriber() {
    engine = new Engine();
    List<Stage> stages = new ArrayList<>();
    stages.add(new Stage.Map(i -> ((int) i) + 1));
    // This graph is not closed - so it's invalid
    Graph graph = new Graph(stages);
    engine.buildSubscriber(graph);
  }

  @Test
  public void testValidCompletion() {
    engine = new Engine();
    List<Stage> stages = new ArrayList<>();
    stages.add(new Stage.PublisherStage(Flowable.empty()));
    stages.add(new Stage.Map(i -> ((int) i) + 1));
    stages.add(Stage.FindFirst.INSTANCE);
    Graph graph = new Graph(stages);
    assertThat(engine.buildCompletion(graph)).isNotNull();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidCompletion() {
    engine = new Engine();
    List<Stage> stages = new ArrayList<>();
    stages.add(new Stage.PublisherStage(Flowable.empty()));
    stages.add(new Stage.Map(i -> ((int) i) + 1));
    // This graph is not closed - so it's invalid
    Graph graph = new Graph(stages);
    engine.buildCompletion(graph);
  }

  @Test(expected = UnsupportedStageException.class)
  public void testCompletionWithUnknownStage() {
    engine = new Engine();
    List<Stage> stages = new ArrayList<>();
    stages.add(new Stage.PublisherStage(Flowable.empty()));
    stages.add(new Stage.Map(i -> ((int) i) + 1));
    stages.add(new Stage() {
      @Override
      public boolean hasInlet() {
        return true;
      }

      @Override
      public boolean hasOutlet() {
        return true;
      }
    });
    stages.add(Stage.FindFirst.INSTANCE);
    Graph graph = new Graph(stages);
    assertThat(engine.buildCompletion(graph)).isNotNull();
  }

  @Test
  public void testValidPublisher() {
    engine = new Engine();
    List<Stage> stages = new ArrayList<>();
    stages.add(new Stage.Of(Arrays.asList(1, 2, 3)));
    stages.add(new Stage.Map(i -> ((int) i) + 1));
    Graph graph = new Graph(stages);
    assertThat(engine.buildPublisher(graph)).isNotNull();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidPublisher() {
    engine = new Engine();
    List<Stage> stages = new ArrayList<>();
    stages.add(new Stage.Map(i -> ((int) i) + 1));
    stages.add(Stage.FindFirst.INSTANCE);
    // This graph is closed, invalid as publisher
    Graph graph = new Graph(stages);
    engine.buildPublisher(graph);
  }

  @Test(expected = UnsupportedStageException.class)
  public void testCreatingPublisherWithUnknownStage() {
    engine = new Engine();
    List<Stage> stages = new ArrayList<>();
    stages.add(new Stage() {
      @Override
      public boolean hasInlet() {
        return false;
      }

      @Override
      public boolean hasOutlet() {
        return true;
      }
    });
    stages.add(new Stage.Map(i -> ((int) i) + 1));
    Graph graph = new Graph(stages);
    engine.buildPublisher(graph);
  }

  @Test
  public void testDefaultVertxInstanceManagement() {
    AtomicBoolean closed = new AtomicBoolean(false);

    Engine engine1 = new Engine();
    Vertx vertx1 = engine1.vertx();
    Engine engine2 = new Engine();
    Vertx vertx2 = engine2.vertx();
    assertThat(Engine.getDefaultVertx()).contains(vertx2).contains(vertx1);

    if (Engine.getDefaultVertx().isPresent()) {
      VertxInternal internal = (VertxInternal) (Engine.getDefaultVertx().get().getDelegate());
      internal.addCloseHook(completionHandler -> {
        closed.set(true);
        if (completionHandler != null) {
          completionHandler.handle(Future.succeededFuture());
        }
      });
    }
    
    engine1.close();
    assertThat(Engine.getDefaultVertx()).isNotEmpty();
    assertThat(closed).isFalse();
    engine2.close();
    assertThat(Engine.getDefaultVertx()).isEmpty();
    assertThat(closed).isTrue();
  }



}