package com.github.davidmoten.structures.btree;

import static com.github.davidmoten.structures.btree.NodeActual.getMedianNumber;
import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Optional;

public class NodeActualTest {

	/**
	 * Given an empty node, degree 3
	 * 
	 * When a key is added
	 * 
	 * Then the result is node with that new key
	 * 
	 */
	@Test
	public void testInsertHere() {
		NodeRef<Integer> node = createNode();
		insert(node, 1);
		checkEquals(node, 1);
	}

	/**
	 * Given a node with 1 key, degree 3
	 * 
	 * When 2 is added
	 * 
	 * Then the result is node with keys 1,2
	 * 
	 */
	@Test
	public void testInsertHere2() {
		NodeRef<Integer> node = createNode();
		insert(node, 1, 2);
		checkEquals(node, 1, 2);
	}

	/**
	 * Given a node with 2 keys, degree 3
	 * 
	 * When 3 is added
	 * 
	 * Then the result is node with keys 1,2,3
	 * 
	 */
	@Test
	public void testInsertHere3() {
		NodeRef<Integer> node = createNode();
		node.insertHere(Key.create(1));
		node.insertHere(Key.create(2));
		node.insertHere(Key.create(3));
		checkEquals(node, 1, 2, 3);
	}

	/**
	 * Given a node with key 1, degree 3
	 * 
	 * When 0 is added
	 * 
	 * Then the result is node with keys 0,1
	 * 
	 */
	@Test
	public void testInsertHere4() {
		NodeRef<Integer> node = createNode();
		insert(node, 1, 0);
		checkEquals(node, 0, 1);
	}

	/**
	 * Given a node with keys 1,3, degree 3
	 * 
	 * When 2 is added
	 * 
	 * Then the result is node with keys 1,2,3
	 * 
	 */
	@Test
	public void testInsertHere5() {
		NodeRef<Integer> node = createNode();
		insert(node, 1, 3, 2);
		checkEquals(node, 1, 2, 3);
	}

	private static <R extends Serializable & Comparable<R>> void insert(
			NodeRef<R> node, R... values) {
		for (R value : values)
			node.insertHere(Key.create(value));
	}

	@Test
	public void testMedianNumber() {
		assertEquals(2, getMedianNumber(3));
		assertEquals(2, getMedianNumber(4));
		assertEquals(3, getMedianNumber(5));
		assertEquals(3, getMedianNumber(6));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMedianNumberFrom2NotAllowed() {
		getMedianNumber(2);
	}

	@Test
	public void testSplit1() {
		NodeRef<Integer> node = createNode();
		insert(node, 1, 2, 3);
		KeyNodes<Integer> result = node.split(KeyNodes.<Integer> create());
		checkEquals(result.getSaveQueue().getLast(), 2);
	}

	private void checkEquals(NodeRef<Integer> node, Integer... values) {
		List<? extends Key<Integer>> keys = node.getKeys();
		for (int i = 0; i < values.length; i++) {
			if (!keys.get(i).value().equals(values[i]))
				Assert.fail("not equal " + node + " " + Arrays.toString(values));
		}
		assertEquals(keys.size(), values.length);
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
