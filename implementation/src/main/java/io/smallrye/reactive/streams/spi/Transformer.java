package io.smallrye.reactive.streams.spi;

import io.reactivex.Flowable;

import java.util.Iterator;
import java.util.ServiceLoader;

public class Transformer {

    private static final Transformer INSTANCE;

    static {
        INSTANCE = new Transformer();
    }

    private final ExecutionModel model;

    private Transformer() {
        ServiceLoader<ExecutionModel> loader = ServiceLoader.load(ExecutionModel.class);
        Iterator<ExecutionModel> iterator = loader.iterator();
        if (iterator.hasNext()) {
            model = iterator.next();
        } else {
            model = i -> i;
        }
    }

    /**
     * Calls the model.
     *
     * @param flowable the flowable
     * @param <T>      the type of data
     * @return the decorated flowable if needed
     */
    @SuppressWarnings("unchecked")
    public static <T> Flowable<T> apply(Flowable<T> flowable) {
        return INSTANCE.model.apply(flowable);
    }

}
