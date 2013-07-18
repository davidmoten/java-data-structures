package com.github.davidmoten.structures.btree;

import java.io.Serializable;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

class AddResult<T extends Serializable & Comparable<T>> {

	// either
	private final Optional<Key<T>> splitKey;
	// or
	private final Optional<Node<T>> node;

	private AddResult(Optional<Key<T>> splitKey, Optional<Node<T>> node) {
		Preconditions.checkArgument(splitKey.isPresent() && !node.isPresent()
				|| !splitKey.isPresent() && node.isPresent());
		this.splitKey = splitKey;
		this.node = node;
	}

	Optional<Key<T>> getSplitKey() {
		return splitKey;
	}

	Optional<Node<T>> getNode() {
		return node;
	}

	static <R extends Serializable & Comparable<R>> AddResult<R> createFromSplitKey(
			Key<R> splitKey) {
		return new AddResult<R>(Optional.of(splitKey),
				Optional.<Node<R>> absent());
	}

	static <R extends Serializable & Comparable<R>> AddResult<R> createFromNonSplitNode(
			Node<R> node) {
		return new AddResult<R>(Optional.<Key<R>> absent(), Optional.of(node));
	}
}
