package com.github.davidmoten.structures.btree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class KeyTest {

    @Test
    public void testHasChild() {
        Key<Double> k = new Key<Double>(1.0);
        Node<Double> left = new Node<Double>(3, null);
        Node<Double> right = new Node<Double>(3, null);

        assertFalse(k.hasChild());
        k.setLeft(left);
        assertTrue(k.hasChild());
        k.setLeft(null);
        k.setRight(right);
        assertTrue(k.hasChild());
    }
}
