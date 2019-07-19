package io.smallrye.reactive.streams.api.impl;

import java.time.Duration;

public class ParameterValidation {

    public static Duration validate(Duration duration, String name) {
        if (duration == null) {
            throw new IllegalArgumentException(String.format("`%s` must not be `null`", name));
        }
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(String.format("`%s` must be greater than zero`", name));
        }
        return duration;
    }

    public static <T> T nonNull(T instance, String name) {
        if (instance == null) {
            throw new IllegalArgumentException(String.format("`%s` must not be `null`", name));
        }
        return instance;
    }

}
