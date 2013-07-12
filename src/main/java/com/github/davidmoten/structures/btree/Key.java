package com.github.davidmoten.structures.btree;

import static com.google.common.base.Optional.absent;

import java.io.Serializable;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

class Key<T extends Serializable & Comparable<T>> implements Serializable {

	private static final long serialVersionUID = 5000199744985500145L;

	private final T t;
	private Optional<Node<T>> left = absent();
	private Optional<Node<T>> right = absent();
	private boolean deleted = false;

	// fields not to be serialized
	private transient Optional<Key<T>> next = absent();
	private transient Optional<Node<T>> node = absent();

	Key(T t) {
		this.t = t;
	}

	boolean isDeleted() {
		return deleted;
	}

	void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	T value() {
		return t;
	}

	Optional<Node<T>> getLeft() {
		return left;
	}

	void setLeft(Optional<Node<T>> left) {
		Preconditions.checkNotNull(left);
		this.left = left;

	}

	Optional<Node<T>> getRight() {
		return right;
	}

	void setRight(Optional<Node<T>> right) {
		Preconditions.checkNotNull(right);
		this.right = right;

	}

	boolean hasChild() {
		return left.isPresent() || right.isPresent();
	}

	@Override
	public String toString() {
		return toString("  ");
	}

	Optional<Node<T>> getParent() {
		return node;
	}

	void setNode(Optional<Node<T>> node) {
		this.node = node;
	}

	Optional<Key<T>> next() {
		return next;
	}

	void setNext(Optional<Key<T>> next) {
		this.next = next;
	}

	String toString(String space) {
		StringBuilder builder = new StringBuilder();
		builder.append("\n" + space + "Key [t=");
		builder.append(t);
		if (node.isPresent()) {
			builder.append(", node=");
			builder.append(node.get().keysAsString());
		}
		if (left.isPresent()) {
			builder.append("\n" + space + "  left=");
			builder.append(left.get().toString(space + "    "));
		}
		if (right.isPresent()) {
			builder.append("\n" + space + "  right=");
			builder.append(right.get().toString(space + "    "));
		}
		if (next.isPresent()) {
			builder.append("\n" + space + "  next=");
			builder.append(next.get().toString(space));
		}
		builder.append("]");
		return builder.toString();
	}

	Optional<Node<T>> getNode() {
		return node;
	}
}
