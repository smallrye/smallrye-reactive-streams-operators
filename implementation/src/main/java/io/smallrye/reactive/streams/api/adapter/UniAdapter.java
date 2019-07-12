package io.smallrye.reactive.streams.api.adapter;

import io.smallrye.reactive.streams.api.Uni;

public interface UniAdapter<O> {

    boolean accept(Class<O> clazz);

    O adaptTo(Uni<?> uni);

    Uni<?> adaptFrom(O instance);

}
