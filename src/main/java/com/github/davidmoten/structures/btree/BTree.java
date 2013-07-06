package com.github.davidmoten.structures.btree;

import java.util.Iterator;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class BTree<T extends Comparable<T>> implements Iterable<T> {

	private Node<T> root;
	private final int degree;

	/**
	 * Constructor.
	 * 
	 * @param degree
	 */
	public BTree(int degree) {
		this.degree = degree;
		Preconditions.checkArgument(degree >= 2, "degree must be >=2");
		root = new NodeLoaded<T>(this);
	}

	public int getDegree() {
		return degree;
	}

	/**
	 * Adds an element to the b-tree. May replace root.
	 * 
	 * @param t
	 */
	public void add(T t) {
		Optional<Node<T>> newRoot = root.add(t);
		if (newRoot.isPresent())
			root = newRoot.get();
	}

	/**
	 * Returns the first T found that equals t from this b-tree.
	 * 
	 * @param t
	 * @return
	 */
	public Optional<T> find(T t) {
		return root.find(t);
	}

	/**
	 * Returns the result of a range query.
	 * 
	 * @param t1
	 * @param t2
	 * @param op1
	 * @param op2
	 * @return
	 */
	public Iterable<Optional<T>> find(T t1, T t2, ComparisonOperator op1,
			ComparisonOperator op2) {
		// TODO
		return null;
	}

	public long delete(T key) {
		return root.delete(key);
	}

	@VisibleForTesting
	List<? extends Key<T>> getKeys() {
		return root.getKeys();
	}

	@Override
	public Iterator<T> iterator() {
		return root.iterator();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BTree [root=");
		builder.append(root);
		builder.append("]");
		return builder.toString();
	}

}
