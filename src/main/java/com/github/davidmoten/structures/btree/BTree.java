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

	private Node<T> root;

	private final long nextPosition = 1000;

	private final int degree;
	private final File file;
	private final long keySize;

	private final Class<T> cls;

	/**
	 * Constructor.
	 * 
	 * @param degree
	 */
	private BTree(int degree, File file, Class<T> cls, long keySize) {
		this.cls = cls;
		Preconditions.checkArgument(degree >= 2, "degree must be >=2");
		long startPosition = nextPosition();
		root = new NodeRef<T>(this, startPosition,
				Optional.<KeySide<T>> absent());
		this.degree = degree;
		this.file = file;
		this.keySize = keySize;
	}

	public static class Builder<R extends Serializable & Comparable<R>> {
		private int degree = 100;
		private File file = new File("target/btree.index");
		private long keySize = 1000;
		private final Class<R> cls;

		public Builder(Class<R> cls) {
			this.cls = cls;
		}

		public Builder<R> degree(int degree) {
			this.degree = degree;
			return this;
		}

		public Builder<R> file(File file) {
			this.file = file;
			return this;
		}

		public Builder<R> keySize(long keySize) {
			this.keySize = keySize;
			return this;
		}

		public BTree<R> build() {
			return new BTree<R>(degree, file, cls, keySize);
		}
	}

	public static <R extends Comparable<R> & Serializable> Builder<R> builder(
			Class<R> cls) {
		return new Builder<R>(cls);
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

	public long nextPosition() {
		return nextPosition;
	}

	public void save(Node<T> node) {

	}

}
