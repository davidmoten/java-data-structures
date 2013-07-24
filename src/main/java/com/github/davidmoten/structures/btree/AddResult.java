package com.github.davidmoten.structures.btree;

import java.io.Serializable;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

class AddResult<T extends Serializable & Comparable<T>> {

	// either
	private final Optional<Key<T>> splitKey;
	// or
	private final Optional<NodeRef<T>> node;

	private AddResult(Optional<Key<T>> splitKey, Optional<NodeRef<T>> node) {
		Preconditions.checkArgument(splitKey.isPresent() && !node.isPresent()
				|| !splitKey.isPresent() && node.isPresent());
		this.splitKey = splitKey;
		this.node = node;
	}

	Optional<Key<T>> getSplitKey() {
		return splitKey;
	}

	Optional<NodeRef<T>> getNode() {
		return node;
	}

	static <R extends Serializable & Comparable<R>> AddResult<R> createFromSplitKey(
			Key<R> splitKey) {
		return new AddResult<R>(Optional.of(splitKey),
				Optional.<NodeRef<R>> absent());
	}

	static <R extends Serializable & Comparable<R>> AddResult<R> createFromNonSplitNode(
			NodeRef<R> node) {
		return new AddResult<R>(Optional.<Key<R>> absent(), Optional.of(node));
	}

	@Override
	public String toString() {
		return "AddResult [splitKey=" + splitKey + ", node=" + node + "]";
	}

}
