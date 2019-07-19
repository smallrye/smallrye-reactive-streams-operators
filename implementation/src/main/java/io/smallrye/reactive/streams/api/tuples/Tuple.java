package io.smallrye.reactive.streams.api.tuples;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public interface Tuple extends Iterable<Object> {

    /**
     * Get the items stored at the given index.
     *
     * @param index The index of the object to retrieve.
     * @return The object, can be {@code null}
     * @throws IndexOutOfBoundsException if the index is greater than the size.
     */
    Object nth(int index);

    /**
     * Gets a {@link java.util.List} of {@link Object} containing the items from this {@link Tuple}
     *
     * @return A list containing the item of the tuple.
     */
    List<Object> asList();

    /**
     * Gets an immutable {@link Iterator}  traversing the content of this {@link Tuple}.
     *
     * @return the iterator
     */
    @Override
    default Iterator<Object> iterator() {
        return Collections.unmodifiableList(asList()).iterator();
    }

    /**
     * @return the number of items stored in the {@link Tuple}
     */
    int size();
}
