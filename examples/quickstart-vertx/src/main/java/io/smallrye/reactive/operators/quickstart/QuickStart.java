package io.smallrye.reactive.operators.quickstart;

import io.vertx.reactivex.core.Vertx;

public class QuickStart {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(DataGenerator.class.getName());
        vertx.deployVerticle(DataProcessor.class.getName());
    }


}
