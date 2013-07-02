package com.github.davidmoten.structures.btree;

import static com.google.common.base.Optional.absent;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class Key<T extends Comparable<T>> {

	private final T t;
	private Optional<Node<T>> left = absent();
	private Optional<Node<T>> right = absent();
	private boolean deleted = false;
	private Optional<Key<T>> next = absent();
	private Optional<Node<T>> node = absent();

	public Key(T t) {
		this.t = t;
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

	public Optional<Node<T>> getLeft() {
		return left;
	}

	public void setLeft(Optional<Node<T>> left) {
		Preconditions.checkNotNull(left);
		this.left = left;
		if (left.isPresent()) {
			left.get().setParentKeySide(
					Optional.of(new KeyAndSide<T>(this, Side.LEFT)));
		}
	}

	public Optional<Node<T>> getRight() {
		return right;
	}

	public void setRight(Optional<Node<T>> right) {
		Preconditions.checkNotNull(right);
		this.right = right;
		if (right.isPresent()) {
			right.get().setParentKeySide(
					Optional.of(new KeyAndSide<T>(this, Side.RIGHT)));
		}
	}

	public boolean hasChild() {
		return left.isPresent() || right.isPresent();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Key [t=");
		builder.append(t);
		if (left.isPresent()) {
			builder.append(", left=");
			builder.append(left.get());
		}
		if (right.isPresent()) {
			builder.append(", right=");
			builder.append(right.get());
		}
		if (next.isPresent()) {
			builder.append(", next=");
			builder.append(next.get());
		}
		builder.append("]");
		return builder.toString();
	}

	public Optional<Node<T>> getParent() {
		return node;
	}

	public void setNode(Optional<Node<T>> node) {
		this.node = node;
	}

	public Optional<Key<T>> next() {
		return next;
	}

	public void setNext(Optional<Key<T>> next) {
		this.next = next;
	}
}
