package com.github.davidmoten.structures.btree;

import static com.google.common.base.Optional.absent;

import java.io.Serializable;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

class Key<T extends Serializable & Comparable<T>> {

	private final T t;
	private Optional<NodeRef<T>> left = absent();
	private Optional<NodeRef<T>> right = absent();
	private boolean deleted = false;

	// fields not to be serialized
	private transient Optional<Key<T>> next = absent();
	private transient Optional<NodeRef<T>> node = absent();

	Key(T t) {
		this.t = t;
	}

	boolean isDeleted() {
		return deleted;
	}

	Key(T t, Optional<NodeRef<T>> left, Optional<NodeRef<T>> right,
			boolean deleted, Optional<NodeRef<T>> node, Optional<Key<T>> next) {
		this.t = t;
		this.left = left;
		this.right = right;
		this.deleted = deleted;
		this.node = node;
		this.next = next;
	}

	void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	T value() {
		return t;
	}

	Optional<NodeRef<T>> getLeft() {
		return left;
	}

	void setLeft(Optional<NodeRef<T>> left) {
		Preconditions.checkNotNull(left);
		this.left = left;

	}

	Optional<NodeRef<T>> getRight() {
		return right;
	}

	void setRight(Optional<NodeRef<T>> right) {
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

	Optional<NodeRef<T>> getParent() {
		return node;
	}

	void setNode(Optional<NodeRef<T>> node) {
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

	Optional<NodeRef<T>> getNode() {
		return node;
	}

	void setSide(Side side, Optional<NodeRef<T>> nd) {
		Preconditions.checkArgument(!Side.TOP.equals(side),
				"side cannot be TOP");
		if (Side.LEFT.equals(side))
			left = nd;
		else
			right = nd;
	}

	Key<T> side(Side side, Optional<NodeRef<T>> nd) {
		Preconditions.checkArgument(!Side.TOP.equals(side),
				"side cannot be TOP");
		if (Side.LEFT.equals(side))
			return new Key<T>(t, nd, right, deleted, node, next);
		else
			return new Key<T>(t, left, nd, deleted, node, next);
	}

	Key<T> node(NodeRef<T> node) {
		return new Key<T>(t, left, right, deleted, Optional.of(node), next);
	}

	Key<T> nodeNext(NodeRef<T> node, Optional<Key<T>> next) {
		return new Key<T>(t, left, right, deleted, Optional.of(node), next);
	}

	Key<T> left(NodeRef<T> node, Optional<NodeRef<T>> left) {
		return new Key<T>(t, left, right, deleted, Optional.of(node), next);
	}

}
