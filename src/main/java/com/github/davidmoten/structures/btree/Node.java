package com.github.davidmoten.structures.btree;

import static com.google.common.base.Optional.absent;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

public class Node<T extends Comparable<T>> {

	private final int degree;
	private Optional<Node<T>> parent;
	private final Optional<Key<T>> first;

	public Node(int degree, Optional<Node<T>> parent) {
		this.degree = degree;
		this.parent = parent;
		this.first = Optional.absent();
	}

	public Node(int degree) {
		this(degree, Optional.<Node<T>> absent());
	}

	/**
	 * Adds the element t to the node. If root node of BTree is changed then
	 * returns new root node otherwise returns null.
	 * 
	 * @param t
	 * @return
	 */
	public Node<T> add(T t) {

		if (!isLeafNode()) {
			return addToNonLeafNode(t);
		} else
			return add(new Key<T>(t));
	}

	private Node<T> addToNonLeafNode(T t) {
		Node<T> result = null;
		boolean added = false;
		Optional<Key<T>> key = first;
		Optional<Key<T>> last = absent();
		while (key.isPresent()) {
			if (t.compareTo(key.get().value()) < 0) {
				// don't need to check that left is non-null because of
				// properties of b-tree
				result = key.get().getLeft().add(t);
				added = true;
				break;
			}
			key = key.get().next();
		}

		if (!added && first.isPresent()) {
			// don't need to check that left is non-null because of properties
			// of b-tree
			result = last.get().getRight().add(t);
		}
		return result;
	}

	/**
	 * Returns true if and only this node is a leaf node (has no children).
	 * Because of the properties of a b-tree only have to check if the first key
	 * has a child.
	 * 
	 * @return
	 */
	private boolean isLeafNode() {

		return getKeys().size() == 0 || !getKeys().get(0).hasChild();
	}

	/**
	 * Inserts key into the list of keys in sorted order. The inserted key has
	 * priority in terms of its children become the children of its neighbours
	 * in the list of keys.
	 * 
	 * @param first
	 * @param key
	 */
	private Key<T> add(Optional<Key<T>> first, Key<T> key) {
		System.out.println("adding " + key + " to " + first);
		Integer addedAtIndex = null;

		Optional<Key<T>> k = first;
		Optional<Key<T>> previous = absent();
		Optional<Key<T>> next = absent();
		while (k.isPresent()) {
			if (key.value().compareTo(k.get().value()) < 0) {
				if (previous.isPresent())
					previous.get().setNext(Optional.of(key));
				key.setNext(k);
				next = k;
				break;
			}
			previous = k;
			k = k.get().next();
		}

		if (!next.isPresent() && previous.isPresent()) {
			previous.get().setNext(Optional.of(key));
		}

		Key<T> result;
		if (!previous.isPresent())
			result = key;
		else
			result = first.get();

		// update previous and following keys to the newly added one
		if (previous.isPresent()) {
			previous.get().setRight(key.getLeft());
		}
		if (next.isPresent()) {
			next.get().setLeft(key.getRight());
		}
		return result;
	}

	private int countKeys() {
		int count = 0;
		Optional<Key<T>> k = first;
		while (k.isPresent()) {
			count++;
			k = k.get().next();
		}
		return count;
	}

	/**
	 * Adds the key to the node. If root node of BTree is changed then returns
	 * new root node otherwise returns this.
	 * 
	 * @param key
	 * @return
	 */
	private Node<T> add(Key<T> key) {

		add(first, key);

		Node<T> result = null;
		int keyCount = countKeys();
		if (keyCount == degree) {
			// split
			if (isRoot()) {
				// creating new root
				parent = Optional.of(new Node<T>(degree));
				result = parent.get();
			}

			int medianIndex = getMedianKeyIndex();

			Key<T> medianKey = keys.get(medianIndex);

			splitKeysEitherSideOfMedianIntoTwoChildrenOfParent(medianIndex);

			keys.remove(medianIndex);

			Node<T> result2 = parent.get().add(medianKey);

			if (result2 != null)
				result = result2;

		} else
			result = null;

		return result;

	}

	/**
	 * Returns true if and only if this is the root node of the BTree (has no
	 * parent).
	 * 
	 * @return
	 */
	private boolean isRoot() {
		return !parent.isPresent();
	}

	private void splitKeysEitherSideOfMedianIntoTwoChildrenOfParent(
			int medianIndex) {
		Key<T> medianKey = keys.get(medianIndex);
		Node<T> child1 = new Node<T>(degree, parent);
		for (int i = 0; i < medianIndex; i++) {
			child1.keys.add(keys.get(i));
		}
		medianKey.setLeft(child1);

		Node<T> child2 = new Node<T>(degree, parent);
		for (int i = medianIndex + 1; i < keys.size(); i++) {
			child2.keys.add(keys.get(i));
		}
		medianKey.setRight(child2);

	}

	private int getMedianKeyIndex() {
		int medianIndex;
		if (keys.size() % 2 == 1)
			medianIndex = keys.size() / 2;
		else
			medianIndex = (keys.size() - 1) / 2;
		return medianIndex;
	}

	@VisibleForTesting
	List<? extends Key<T>> getKeys() {
		return keys;
	}

	Key<T> getKey(int index) {
		return keys.get(index);
	}

	public Optional<T> find(T t) {
		boolean isLeaf = isLeafNode();
		for (int i = 0; i < keys.size(); i++) {
			Key<T> key = keys.get(i);
			int compare = t.compareTo(keys.get(i).value());
			if (compare < 0) {
				if (isLeaf)
					return absent();
				else
					return key.getLeft().find(t);
			} else if (compare == 0 && !key.isDeleted())
				return Optional.of(key.value());
		}
		if (!isLeaf) {
			Node<T> right = keys.get(keys.size() - 1).getRight();
			if (right != null)
				return right.find(t);
			else
				return absent();
		} else
			return absent();
	}

	/**
	 * Marks all keys as deleted that equal t.
	 * 
	 * @param t
	 * @return
	 */
	public long delete(T t) {
		boolean isLeaf = isLeafNode();
		int count = 0;
		for (int i = 0; i < keys.size(); i++) {
			Key<T> key = keys.get(i);
			int compare = t.compareTo(keys.get(i).value());
			if (compare < 0) {
				if (isLeaf)
					return 0;
				else
					return key.getLeft().delete(t);
			} else if (compare == 0) {
				key.setDeleted(true);
				count++;
			}
		}
		if (count > 0)
			return count;
		else if (!isLeaf) {
			Node<T> right = keys.get(keys.size() - 1).getRight();
			if (right != null)
				return right.delete(t);
			else
				return 0;
		} else
			return 0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Node [keys=");
		builder.append(keys);
		builder.append("]");
		return builder.toString();
	}

}
