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
class AddResult2<T extends Serializable & Comparable<T>> {

	private final Optional<Key<T>> splitKey;

	private final LinkedList<NodeRef<T>> saveQueue;

	private AddResult2(Optional<Key<T>> splitKey,
			LinkedList<NodeRef<T>> saveQueue) {
		Preconditions.checkNotNull(splitKey);
		Preconditions.checkNotNull(saveQueue);
		this.splitKey = splitKey;
		this.saveQueue = Lists.newLinkedList(saveQueue);
	}

	Optional<Key<T>> getSplitKey() {
		return splitKey;
	}

	LinkedList<NodeRef<T>> getSaveQueue() {
		return saveQueue;
	}

	static <R extends Serializable & Comparable<R>> AddResult2<R> createFromSplitKey(
			Key<R> splitKey, LinkedList<NodeRef<R>> saveQueue) {
		return new AddResult2<R>(Optional.of(splitKey), saveQueue);
	}

	@Override
	public String toString() {
		return "AddResult2 [splitKey=" + splitKey + ", saveQueue=" + saveQueue
				+ "]";
	}

}
