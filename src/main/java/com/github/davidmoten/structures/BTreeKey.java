package com.github.davidmoten.structures;

public class BTreeKey<T extends Comparable<T>> {
	private final T t;

	private BTreeNode<T> left;
	private BTreeNode<T> right;

	public BTreeKey(T t) {
		this.t = t;
	}

	public T value() {
		return t;
	}

	public BTreeNode<T> getLeft() {
		return left;
	}

	public void setLeft(BTreeNode<T> left) {
		this.left = left;
	}

	public BTreeNode<T> getRight() {
		return right;
	}

	public void setRight(BTreeNode<T> right) {
		this.right = right;
	}
}
