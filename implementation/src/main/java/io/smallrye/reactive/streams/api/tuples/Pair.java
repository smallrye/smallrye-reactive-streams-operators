package io.smallrye.reactive.streams.api.tuples;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A tuple containing two item.
 *
 * @param <L> The type of the first item
 * @param <R> The type of the second item
 */
public class Pair<L, R> implements Tuple {

    final L item1;
    final R item2;

    protected Pair(L left, R right) {
        this.item1 = left;
        this.item2 = right;
    }

    public static <L, R> Pair<L, R> of(L l, R r) {
        return new Pair<>(l, r);
    }

    /**
     * Gets the first item.
     *
     * @return The first item, can be {@code null}
     */
    public L getItem1() {
        return item1;
    }

    /**
     * Gets the second item.
     *
     * @return The second item, can be {@code null}
     */
    public R getItem2() {
        return item2;
    }

    /**
     * Applies a mapper function to the left (item1) part of this {@link Pair} to produce a new {@link Pair}.
     * The right part (item2) is not modified.
     *
     * @param mapper the mapping {@link Function} for the left item
     * @param <T>    the new type for the left item
     * @return the new {@link Pair}
     */
    public <T> Pair<T, R> mapItem1(Function<L, T> mapper) {
        return Pair.of(mapper.apply(item1), item2);
    }

    /**
     * Applies a mapper function to the right part (item2) of this {@link Pair} to produce a new {@link Pair}.
     * The left (item1) part is not modified.
     *
     * @param mapper the mapping {@link Function} for the right item
     * @param <T>    the new type for the right item
     * @return the new {@link Pair}
     */
    public <T> Pair<L, T> mapItem2(Function<R, T> mapper) {
        return Pair.of(item1, mapper.apply(item2));
    }

    @Override
    public Object nth(int index) {
        assertIndexInBounds(index);
        if (index == 0) {
            return item1;
        }
        return item2;
    }

    protected void assertIndexInBounds(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException(
                    "Cannot retrieve item at position " + index + ", size is " + size());
        }
    }

    @Override
    public List<Object> asList() {
        return Arrays.asList(item1, item2);
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

        if (!Objects.equals(item1, pair.item1)) {
            return false;
        }
        return Objects.equals(item2, pair.item2);
    }

    @Override
    public int hashCode() {
        int result = item1 != null ? item1.hashCode() : 0;
        result = 31 * result + (item2 != null ? item2.hashCode() : 0);
        return result;
    }

    public int size() {
        return 2;
    }

    @Override
    public String toString() {
        return "Pair{left=" + item1 + ", right=" + item2 + '}';
    }
}
