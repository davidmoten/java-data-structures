package com.github.davidmoten.structures.btree;

import static com.github.davidmoten.structures.btree.BTree.builder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.base.Optional;

public class KeyTest {

	@Test
	public void testHasChild() {
		BTree<Double> tree = builder(Double.class).degree(3).build();
		Optional<NodeRef<Double>> absent = Optional.absent();

		Key<Double> k = new Key<Double>(1.0);
		NodeRef<Double> left = new NodeRef<Double>(tree,
				Optional.<Long> absent());
		NodeRef<Double> right = new NodeRef<Double>(tree,
				Optional.<Long> absent());

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
