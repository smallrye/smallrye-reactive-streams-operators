package io.smallrye.reactive.streams.api;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PairTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testWithStrings() {
        Pair<String, String> pair = Pair.of("a", "b");
        assertThat(pair.left()).isEqualTo("a");
        assertThat(pair.right()).isEqualTo("b");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testWithStringAndInt() {
        Pair<String, Integer> pair = Pair.of("a", 1);
        assertThat(pair.left()).isEqualTo("a");
        assertThat(pair.right()).isEqualTo(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreationWithNullValues() {
        Pair<String, String> pair = Pair.of(null, null);
        assertThat(pair.left()).isNull();
        assertThat(pair.right()).isNull();
    }

    @Test
    public void testEqualsWithStringAndInteger() {
        Pair<String, Integer> pair = Pair.of("a", 1);
        assertThat(pair).isEqualTo(pair);
        assertThat(pair).isNotEqualTo(null);
        assertThat(pair).isNotEqualTo("not a pair");
    }

    @Test
    public void testEqualsWithStringAndNull() {
        Pair<String, Integer> pair = Pair.of("a", null);
        assertThat(pair).isEqualTo(pair);
        assertThat(pair).isNotEqualTo(null);
        assertThat(pair).isNotEqualTo("not a pair");
        assertThat(pair).isEqualTo(Pair.of("a", null));
        assertThat(pair).isNotEqualTo(Pair.of(null, null));
    }

    @Test
    public void testHashCode() {
        Pair<String, Integer> pair = Pair.of("a", null);
        assertThat(pair.hashCode()).isEqualTo(pair.hashCode());
        assertThat(pair.hashCode()).isNotEqualTo("not a pair".hashCode());
    }

}