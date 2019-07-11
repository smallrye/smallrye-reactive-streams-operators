package io.smallrye.reactive.streams.api;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TupleTest {

    @Test
    public void testWithStrings() {
        Tuple<String> tuple = Tuple.tuple("a", "b", "c");
        assertThat(tuple.size()).isEqualTo(3);
        assertThat(tuple.iterator()).toIterable().containsExactly("a", "b", "c");
        assertThat(tuple.nth(0)).isEqualTo("a");
        assertThat(tuple.nth(1)).isEqualTo("b");
        assertThat(tuple.nth(2)).isEqualTo("c");
        assertThat(tuple.indexOf("b")).isEqualTo(1);
        assertThat(tuple.lastIndexOf("a")).isEqualTo(0);
        assertThat(tuple.lastIndexOf("d")).isEqualTo(-1);
        assertThat(tuple.containsAll("a", "b")).isTrue();
        assertThat(tuple.containsAll("a", "b", "c", "d")).isFalse();
        assertThat(tuple.asList()).containsExactly("a", "b", "c");
    }

    @Test
    public void testFromNull() {
        Tuple<String> tuple = Tuple.tuple((String[]) null);
        assertThat(tuple.size()).isEqualTo(0);
    }

    @Test
    public void testCreationFromArray() {
        Tuple<String> tuple = Tuple.fromArray(new String[]{"a", "b"});
        assertThat(tuple.size()).isEqualTo(2);
    }


    @Test
    public void testThatTuplesCanContainNull() {
        Tuple<String> tuple = Tuple.tuple("a", null, "c");
        assertThat(tuple.size()).isEqualTo(3);
        assertThat(tuple.iterator()).toIterable().containsExactly("a", null, "c");
        assertThat(tuple.nth(0)).isEqualTo("a");
        assertThat(tuple.nth(1)).isNull();
        assertThat(tuple.nth(2)).isEqualTo("c");
        assertThat(tuple.indexOf("b")).isEqualTo(-1);
        assertThat(tuple.asList()).containsExactly("a", null, "c");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testThatWeCannotAccessAboveTheSize() {
        Tuple.tuple("a", "b").nth(3);
    }

    @Test
    public void testEquals() {
        Tuple<String> tuple = Tuple.tuple("a", "b", null);
        assertThat(tuple).isEqualTo(tuple);
        assertThat(tuple).isNotEqualTo(null);
        assertThat(tuple).isNotEqualTo("not a tuple");
        assertThat(tuple).isEqualTo(Tuple.tuple("a", "b", null));
        assertThat(tuple).isNotEqualTo(Tuple.tuple("a", "b", "c"));
        assertThat(Tuple.tuple("a", "b", "c")).isNotEqualTo(tuple);
    }

    @Test
    public void testHashCode() {
        Tuple<String> tuple = Tuple.tuple("a", "b", null);
        assertThat(tuple.hashCode()).isEqualTo(tuple.hashCode());
        assertThat(tuple.hashCode()).isNotEqualTo("not a tuple".hashCode());
        assertThat(tuple.hashCode()).isEqualTo(Tuple.tuple("a", "b", null).hashCode());
        assertThat(tuple.hashCode()).isNotEqualTo(Tuple.tuple("a", "b", "c").hashCode());
        assertThat(Tuple.tuple("a", "b", "c").hashCode()).isNotEqualTo(tuple.hashCode());
    }

    @Test
    public void testWithClasses() {
        Tuple<Animal> tuple = Tuple.tuple(new Dog("indy"), new Rabbit("neo"));
        assertThat(tuple.nth(0).name()).isEqualTo("indy");
        Dog dog = tuple.get(0);
        assertThat(dog.name()).isEqualTo("indy");
    }


    static class Animal {
        private final String name;

        Animal(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }
    }

    static class Dog extends Animal {

        Dog(String name) {
            super(name);
        }
    }

    static class Rabbit extends Animal {

        Rabbit(String name) {
            super(name);
        }
    }

}