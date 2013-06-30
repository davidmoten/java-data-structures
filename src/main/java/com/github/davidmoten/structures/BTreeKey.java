package com.github.davidmoten.structures;

public class BTreeKey<T extends Comparable<T>> implements
		Comparable<BTreeKey<T>> {

	public static <R extends Comparable<R>> BTreeKey<R> max(Class<R> cls) {
		return new BTreeKey<R>(null);
	}

	private final T t;

	private BTreeNode<T> before;

	public BTreeKey(T t) {
		this.t = t;
	}

	public T value() {
		return t;
	}

	public BTreeNode<T> getBefore() {
		return before;
	}

	public void setLeft(BTreeNode<T> left) {
		this.before = left;
	}

	@Override
	public int compareTo(BTreeKey<T> o) {
		if (isMax(o))
			return -1;
		else
			return this.value().compareTo(o.value());
	}

	private boolean isMax(BTreeKey<T> o) {
		return o.value() == null;
	}

}
