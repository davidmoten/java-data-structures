package com.github.davidmoten.structures.btree;

import java.io.Serializable;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

class AddResult<T extends Serializable & Comparable<T>> {

	// either
	private final Optional<Node<T>> splitNode;
	// or
	private final Optional<Node<T>> node;

	private AddResult(Optional<Node<T>> splitNode, Optional<Node<T>> node) {
		Preconditions.checkArgument(splitNode.isPresent() && !node.isPresent()
				|| !splitNode.isPresent() && node.isPresent());
		this.splitNode = splitNode;
		this.node = node;
	}

	Optional<Node<T>> getSplitNode() {
		return splitNode;
	}

	Optional<Node<T>> getNode() {
		return node;
	}

	static <R extends Serializable & Comparable<R>> AddResult<R> createFromSplitNode(
			Node<R> splitNode) {
		return new AddResult<R>(Optional.of(splitNode),
				Optional.<Node<R>> absent());
	}

	static <R extends Serializable & Comparable<R>> AddResult<R> createFromNonSplitNode(
			Node<R> node) {
		return new AddResult<R>(Optional.<Node<R>> absent(), Optional.of(node));
	}
}
