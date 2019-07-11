package io.smallrye.reactive.streams.api;

import java.util.Map;

/**
 * Represents a of, which is a tuple of 2 items. Note that, unlike {@link Tuple}, pairs can contain items from
 * different types.
 * <p>
 * Values contained in a pair can be {@code null}.
 */
public class Pair<L, R> extends Tuple
        implements Map.Entry<L, R> {

    private final L left;
    private final R right;

    private Pair(L left, R right) {
        super(left, right);
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
    public L getKey() {
        return left;
    }

    @Override
    public R getValue() {
        return right;
    }

    @Override
    public R setValue(R value) {
        throw new UnsupportedOperationException("Pairs are immutable");
    }

    /**
     * Returns a suitable hash code for Pair.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Compares this of to another based on the two elements.
     *
     * @param obj the object to compare to, null returns false
     * @return true if the elements of the of are equal
     */
    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }


}
