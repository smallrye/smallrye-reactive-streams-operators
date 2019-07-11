package io.smallrye.reactive.streams.api;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PairTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testWithStrings() {
        Pair<String, String> pair = Pair.of("a", "b");
        assertThat(pair.size()).isEqualTo(2);
        assertThat(pair.iterator()).toIterable().containsExactly("a", "b");
        assertThat(pair.nth(0)).isEqualTo("a");
        assertThat(pair.nth(1)).isEqualTo("b");
        assertThat(pair.left()).isEqualTo("a");
        assertThat(pair.right()).isEqualTo("b");
        assertThat(pair.getKey()).isEqualTo("a");
        assertThat(pair.getValue()).isEqualTo("b");
        assertThat(pair.indexOf("b")).isEqualTo(1);
        assertThat(pair.lastIndexOf("a")).isEqualTo(0);
        assertThat(pair.lastIndexOf("d")).isEqualTo(-1);
        assertThat(pair.containsAll("a", "b")).isTrue();
        assertThat(pair.containsAll("a", "b", "c", "d")).isFalse();
        assertThat(pair.asList()).containsExactly("a", "b");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testWithStringAndInt() {
        Pair<String, Integer> pair = Pair.of("a", 1);
        assertThat(pair.size()).isEqualTo(2);
        assertThat(pair.iterator()).toIterable().containsExactly("a", 1);
        assertThat(pair.nth(0)).isEqualTo("a");
        assertThat(pair.nth(1)).isEqualTo(1);
        assertThat(pair.left()).isEqualTo("a");
        assertThat(pair.right()).isEqualTo(1);
        assertThat(pair.getKey()).isEqualTo("a");
        assertThat(pair.getValue()).isEqualTo(1);
        assertThat(pair.indexOf(1)).isEqualTo(1);
        assertThat(pair.lastIndexOf("a")).isEqualTo(0);
        assertThat(pair.lastIndexOf("d")).isEqualTo(-1);
        assertThat(pair.containsAll("a", 1)).isTrue();
        assertThat(pair.containsAll("a", 1, "c", "d")).isFalse();
        assertThat(pair.asList()).containsExactly("a", 1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreationWithNullValues() {
        Pair<String, String> pair = Pair.of(null, null);
        assertThat(pair).containsExactly(null, null);
        assertThat(pair.left()).isNull();
        assertThat(pair.right()).isNull();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testThatWeCannotAccessAboveTheSize() {
        Pair.of("a", "b").nth(3);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testThatWeCannotChangeTheValue() {
        Pair.of("a", "b").setValue("c");
    }

    @Test
    public void testEqualsWithStringAndInteger() {
        Pair<String, Integer> pair = Pair.of("a", 1);
        assertThat(pair).isEqualTo(pair);
        assertThat(pair).isNotEqualTo(null);
        assertThat(pair).isNotEqualTo("not a tuple");
        assertThat(pair).isEqualTo(Tuple.tuple("a", 1));
        assertThat(pair).isNotEqualTo(Tuple.tuple(1, "a"));
        assertThat(pair).isNotEqualTo(Tuple.tuple("a", "b", "c"));
        assertThat(pair).isEqualTo(Tuple.tuple("a", 1));
    }

    @Test
    public void testEqualsWithStringAndNull() {
        Pair<String, Integer> pair = Pair.of("a", null);
        assertThat(pair).isEqualTo(pair);
        assertThat(pair).isNotEqualTo(null);
        assertThat(pair).isNotEqualTo("not a tuple");
        assertThat(pair).isEqualTo(Pair.of("a", null));
        assertThat(pair).isNotEqualTo(Pair.of(null, null));
        assertThat(pair).isNotEqualTo(Tuple.tuple("a", "b", "c"));
        assertThat(pair).isEqualTo(Tuple.tuple("a", null));
    }

    @Test
    public void testHashCode() {
        Pair<String, Integer> pair = Pair.of("a", null);
        assertThat(pair.hashCode()).isEqualTo(pair.hashCode());
        assertThat(pair.hashCode()).isNotEqualTo("not a tuple".hashCode());
        assertThat(pair.hashCode()).isEqualTo(Tuple.tuple("a", null).hashCode());
        assertThat(pair.hashCode()).isNotEqualTo(Tuple.tuple("a", "b", "c").hashCode());
        assertThat(Tuple.tuple("a", "b", "c").hashCode()).isNotEqualTo(pair.hashCode());
    }

}