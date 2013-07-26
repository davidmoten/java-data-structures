package com.github.davidmoten.structures.btree;

import java.io.Serializable;
import java.util.LinkedList;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * The result of adding a value to a node.
 * 
 * @author dxm
 * 
 * @param <T>
 */
class AddResult<T extends Serializable & Comparable<T>> {

	// either
	private final Optional<Key<T>> splitKey;
	// or
	private final Optional<NodeRef<T>> node;

	private final LinkedList<NodeRef<T>> saveQueue;

	private AddResult(Optional<Key<T>> splitKey, Optional<NodeRef<T>> node,
			LinkedList<NodeRef<T>> saveQueue) {
		Preconditions.checkArgument(splitKey.isPresent() && !node.isPresent()
				|| !splitKey.isPresent() && node.isPresent());
		this.splitKey = splitKey;
		this.node = node;
		this.saveQueue = Lists.newLinkedList(saveQueue);
	}

	Optional<Key<T>> getSplitKey() {
		return splitKey;
	}

	Optional<NodeRef<T>> getNode() {
		return node;
	}

	LinkedList<NodeRef<T>> getSaveQueue() {
		return saveQueue;
	}

	static <R extends Serializable & Comparable<R>> AddResult<R> createFromSplitKey(
			Key<R> splitKey, LinkedList<NodeRef<R>> saveQueue) {
		return new AddResult<R>(Optional.of(splitKey),
				Optional.<NodeRef<R>> absent(), saveQueue);
	}

	static <R extends Serializable & Comparable<R>> AddResult<R> createFromNonSplitNode(
			NodeRef<R> node, LinkedList<NodeRef<R>> saveQueue) {
		return new AddResult<R>(Optional.<Key<R>> absent(), Optional.of(node),
				saveQueue);
	}

	@Override
	public String toString() {
		return "AddResult [splitKey=" + splitKey + ", node=" + node + "]";
	}

}
