package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.utils.Casts;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

import static io.smallrye.reactive.streams.utils.CompletionStageToPublisher.fromCompletionStage;

public class FromCompletionStageFactory implements PublisherStageFactory<Stage.FromCompletionStage> {

    @Override
    public <OUT> PublisherStage<OUT> create(Engine engine, Stage.FromCompletionStage stage) {
        Objects.requireNonNull(stage);
        return () -> {
            CompletionStage<OUT> cs = Casts.cast(stage.getCompletionStage());
            return Flowable.fromPublisher(fromCompletionStage(cs, false));
        };
    }

}
