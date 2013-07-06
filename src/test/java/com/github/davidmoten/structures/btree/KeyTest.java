package com.github.davidmoten.structures.btree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.base.Optional;

public class KeyTest {

    @Test
    public void testHasChild() {
        Optional<Node<Double>> absent = Optional.<Node<Double>> absent();

        Key<Double> k = new Key<Double>(1.0);
        Node<Double> left = new Node<Double>(3,
                Optional.<KeySide<Double>> absent());
        Node<Double> right = new Node<Double>(3,
                Optional.<KeySide<Double>> absent());

        assertFalse(k.hasChild());
        k.setLeft(Optional.of(left));
        k.setRight(absent);
        assertTrue(k.hasChild());
        k.setLeft(absent);
        k.setRight(Optional.of(right));
        assertTrue(k.hasChild());
        k.setLeft(Optional.of(left));
        k.setRight(Optional.of(right));
        assertTrue(k.hasChild());
    }
}
