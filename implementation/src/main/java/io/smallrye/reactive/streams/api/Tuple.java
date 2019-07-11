package io.smallrye.reactive.streams.api;

import java.util.*;

/**
 * A tuple that holds multiple non-null values.
 * For tuple of two values, see {@link Pair}.
 *
 * This tuple is homogeneous, in the sense that all values belongs to the same type. Values can be {@code null}.
 *
 */
public class Tuple<T> implements Iterable<T> {

    private List<T> items;

    Tuple(T... items) {
        if (items == null) {
            this.items = Collections.emptyList();
        } else {
            this.items = Collections.unmodifiableList(Arrays.asList(items));
        }
    }

    public static <T> Tuple<T> tuple(T... items) {
        return new Tuple<>(items);
    }

    public static <T> Tuple<T> fromArray(T[] obj) {
        return new Tuple<>(obj);
    }

    public int size() {
        return items.size();
    }


    public T nth(int pos) {
        if (pos >= size()) {
            throw new IndexOutOfBoundsException(
                    "Cannot retrieve item " + pos + " in tuple, size is " + size());
        }
        return items.get(pos);
    }

    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }

    public boolean contains(T value) {
        Objects.requireNonNull(value, "The given value cannot be `null`");
        for (T val : this.items) {
            if (itemEquals(value, val)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAll(Collection<T> collection) {
        for (T value : collection) {
            if (!contains(value)) {
                return false;
            }
        }
        return true;
    }


    public boolean containsAll(final T... values) {
        return containsAll(Arrays.asList(values));
    }


    public int indexOf(T value) {
        Objects.requireNonNull(value, "The given value cannot be `null`");
        int i = 0;
        for (T val : items) {
            if (itemEquals(value, val)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private boolean itemEquals(T value, T val) {
        // value cannot be null.
        return value.equals(val);
    }


    public int lastIndexOf(T value) {
        Objects.requireNonNull(value, "The given value cannot be `null`");
        for (int i = size() - 1; i >= 0; i--) {
            final T val = items.get(i);
            if (itemEquals(value, val)) {
                return i;
            }
        }
        return -1;
    }

    public List<T> asList() {
        return items;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((this.items == null) ? 0 : this.items.hashCode());
        return result;
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (! (obj instanceof Tuple)) {
            return false;
        }
        final Tuple other = (Tuple) obj;
        return this.items.equals(other.items);
    }

    @SuppressWarnings("unchecked")
    public <O> O get(int i) {
        return (O) nth(i);
    }
}
