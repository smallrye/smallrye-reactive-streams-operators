package io.smallrye.reactive.streams.api;

import java.util.Objects;

/**
 * Represents a of, which is a tuple of 2 items. Note that, unlike {@link Tuple}, pairs can contain items from
 * different types.
 * <p>
 * Values contained in a pair can be {@code null}.
 */
public class Pair<L, R> {

    private final L left;
    private final R right;

    private Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(L l, R r) {
        return new Pair<>(l, r);
    }

    public L left() {
        return left;
    }

    public R right() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pair)) {
            return false;
        }

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (!Objects.equals(left, pair.left)) {
            return false;
        }
        return Objects.equals(right, pair.right);
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }
}
