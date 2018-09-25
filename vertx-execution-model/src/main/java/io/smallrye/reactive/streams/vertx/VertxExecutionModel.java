package io.smallrye.reactive.streams.vertx;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.spi.ExecutionModel;
import io.vertx.reactivex.core.Context;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;

/**
 * An implementation of {@link ExecutionModel} enforcing the Vert.x execution model.
 */
public class VertxExecutionModel implements ExecutionModel {

    @Override
    public Flowable transform(Flowable input) {
        Context context = Vertx.currentContext();
        if (context != null && context.getDelegate() != null) {
            return input.compose(f -> f.observeOn(RxHelper.scheduler(context)));
        }
        return input;
    }

}
