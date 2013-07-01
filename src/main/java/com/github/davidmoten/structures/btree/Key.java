package com.github.davidmoten.structures.btree;

import com.google.common.base.Optional;

public class Key<T extends Comparable<T>> {

	private final T t;
	private Node<T> left;
	private Node<T> right;
	private boolean deleted;
	private Optional<Key<T>> next;

	public Optional<Key<T>> next() {
		return next;
	}

	public void setNext(Optional<Key<T>> next) {
		this.next = next;
	}

	public Key(T t) {
		this.t = t;
		this.deleted = false;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public T value() {
		return t;
	}

	public Node<T> getLeft() {
		return left;
	}

	public void setLeft(Node<T> left) {
		this.left = left;
	}

	public Node<T> getRight() {
		return right;
	}

	public void setRight(Node<T> right) {
		this.right = right;
	}

	public boolean hasChild() {
		return left != null || right != null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Key [t=");
		builder.append(t);
		if (left != null) {
			builder.append(", left=");
			builder.append(left);
		}
		if (right != null) {
			builder.append(", right=");
			builder.append(right);
		}
		builder.append("]");
		return builder.toString();
	}

}
