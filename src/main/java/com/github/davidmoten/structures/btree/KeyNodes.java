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
class KeyNodes<T extends Serializable & Comparable<T>> {

	private final Optional<Key<T>> key;

	private final LinkedList<NodeRef<T>> saveQueue;

	private KeyNodes(Optional<Key<T>> key, LinkedList<NodeRef<T>> saveQueue) {
		Preconditions.checkNotNull(key);
		Preconditions.checkNotNull(saveQueue);
		this.key = key;
		this.saveQueue = Lists.newLinkedList(saveQueue);
	}

	Optional<Key<T>> getKey() {
		return key;
	}

	LinkedList<NodeRef<T>> getSaveQueue() {
		return saveQueue;
	}

	KeyNodes<T> key(Key<T> key) {
		return new KeyNodes<T>(Optional.of(key), saveQueue);
	}

	KeyNodes<T> add(NodeRef<T> node) {
		LinkedList<NodeRef<T>> list = Lists.newLinkedList(saveQueue);
		list.add(node);
		return new KeyNodes<T>(Optional.<Key<T>> absent(), list);
	}

	static <R extends Serializable & Comparable<R>> KeyNodes<R> create(
			Key<R> key, LinkedList<NodeRef<R>> saveQueue) {
		return new KeyNodes<R>(Optional.of(key), saveQueue);
	}

	static <R extends Serializable & Comparable<R>> KeyNodes<R> create(
			Key<R> key) {
		return new KeyNodes<R>(Optional.of(key),
				Lists.<NodeRef<R>> newLinkedList());
	}

	static <R extends Serializable & Comparable<R>> KeyNodes<R> create(R value) {
		return new KeyNodes<R>(Optional.of(Key.create(value)),
				Lists.<NodeRef<R>> newLinkedList());
	}

	@Override
	public String toString() {
		return "KeyNodes[key=" + key + ", saveQueue=" + saveQueue + "]";
	}

	static <R extends Serializable & Comparable<R>> KeyNodes<R> create() {
		return new KeyNodes<R>(Optional.<Key<R>> absent(),
				Lists.<NodeRef<R>> newLinkedList());
	}

}
