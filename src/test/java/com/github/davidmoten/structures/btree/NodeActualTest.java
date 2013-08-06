package com.github.davidmoten.structures.btree;

import org.junit.Test;

import com.google.common.base.Optional;

public class NodeActualTest {

	/**
	 * Given an empty node, degree 3
	 * 
	 * When a key is added
	 * 
	 * Then the result is a new
	 * 
	 */
	@Test
	public void testAddToLeafNode() {
		NodeRef<Integer> node = createNode();
	}

	private NodeRef<Integer> createNode() {
		NodeListener<Integer> listener = new NodeListener<Integer>() {

			@Override
			public void addToSaveQueue(NodeRef<Integer> node) {
				// TODO Auto-generated method stub

			}

			@Override
			public void load(NodeRef<Integer> node) {
				// TODO Auto-generated method stub

			}
		};
		return new NodeRef<Integer>(listener, Optional.<Long> absent(), 3);
	}
}
