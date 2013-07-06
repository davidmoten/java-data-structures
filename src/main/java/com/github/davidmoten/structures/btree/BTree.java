package com.github.davidmoten.structures.btree;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class BTree<T extends Serializable & Comparable<T>> implements
		Iterable<T>, Serializable {

	private static final long serialVersionUID = -1738319993570666751L;

	private final int degree;
	private Node<T> root;
	private File file;

	/**
	 * Constructor.
	 * 
	 * @param degree
	 */
	public BTree(int degree, File file) {
		Preconditions.checkArgument(degree >= 2, "degree must be >=2");
		this.degree = degree;
		this.file = file;
		long startPosition = 0;
		root = new NodeRef<T>(this, startPosition,
				Optional.<KeySide<T>> absent());
	}

	public BTree(int degree) {
		this(degree, createTempFile());
	}

	private static File createTempFile() {
		try {
			return File.createTempFile("btree", "index");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
