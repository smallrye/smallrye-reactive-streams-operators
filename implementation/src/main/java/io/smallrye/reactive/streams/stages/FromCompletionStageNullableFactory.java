package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.operators.PublisherStage;
import io.smallrye.reactive.streams.operators.PublisherStageFactory;
import io.smallrye.reactive.streams.utils.Casts;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

import static io.smallrye.reactive.streams.utils.CompletionStageToPublisher.fromCompletionStage;

public class FromCompletionStageNullableFactory implements PublisherStageFactory<Stage.FromCompletionStageNullable> {


    @Override
    public <O> PublisherStage<O> create(Engine engine, Stage.FromCompletionStageNullable stage) {
        Objects.requireNonNull(stage);
        return () -> {
            CompletionStage<O> cs = Casts.cast(Objects.requireNonNull(stage.getCompletionStage()));
            return Flowable.fromPublisher(fromCompletionStage(cs, true));
        };
    }

}
