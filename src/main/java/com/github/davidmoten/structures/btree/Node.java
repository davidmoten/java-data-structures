package com.github.davidmoten.structures.btree;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

import java.util.Iterator;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class Node<T extends Comparable<T>> implements Iterable<T> {

	private final int degree;
	private Optional<Node<T>> parent;
	private Optional<Key<T>> first = Optional.absent();
	private Optional<KeyAndSide<T>> parentKeySide = Optional.absent();

	/**
	 * Constructor.
	 * 
	 * @param degree
	 * @param parent
	 */
	public Node(int degree, Optional<Node<T>> parent) {
		Preconditions.checkNotNull(parent);
		this.degree = degree;
		this.parent = parent;
	}

	/**
	 * Constructor. Should be used to create root node only.
	 * 
	 * @param degree
	 */
	public Node(int degree) {
		this(degree, Optional.<Node<T>> absent());
	}

	/**
	 * Adds the element t to the node. If root node of BTree is changed then
	 * returns new root node otherwise returns {@link Optional}.absent().
	 * 
	 * @param t
	 * @return
	 */
	public Optional<Node<T>> add(T t) {

		if (isLeafNode()) {
			return add(new Key<T>(t));
		} else
			return addToNonLeafNode(t);
	}

	/**
	 * Adds the element to the node. If root node of BTree is changed then
	 * returns new root node otherwise returns {@link Optional}.absent().
	 * 
	 * @param t
	 * @return
	 */
	private Optional<Node<T>> addToNonLeafNode(T t) {
		// Note that first will be present because if is internal (non-leaf)
		// node then it must have some keys
		Optional<Node<T>> result = absent();
		boolean added = false;
		Optional<Key<T>> key = first;
		Optional<Key<T>> last = first;
		while (key.isPresent()) {
			if (t.compareTo(key.get().value()) < 0) {
				// don't need to check that left is present because of
				// properties of b-tree
				result = key.get().getLeft().get().add(t);
				added = true;
				break;
			}
			last = key;
			key = key.get().next();
		}

		if (!added) {
			// don't need to check that left is present because of properties
			// of b-tree
			result = last.get().getRight().get().add(t);
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

		return !first.isPresent() || !first.get().hasChild();
	}

	/**
	 * Inserts key into the list of keys in sorted order. The inserted key has
	 * priority in terms of its children become the children of its neighbours
	 * in the list of keys. This method does not do splitting of keys, the key
	 * is guaranteed to be added against this node.
	 * 
	 * @param first
	 *            will always have a value
	 * @param key
	 */
	private Key<T> add(Key<T> first, Key<T> key) {

		// key is not on the current node
		key.setNode(of(this));

		// insert key
		Optional<Key<T>> k = of(first);
		Optional<Key<T>> previous = absent();
		Optional<Key<T>> next = absent();
		while (k.isPresent()) {
			if (key.value().compareTo(k.get().value()) < 0) {
				if (previous.isPresent())
					previous.get().setNext(of(key));
				key.setNext(k);
				next = k;
				break;
			}
			previous = k;
			k = k.get().next();
		}

		if (!next.isPresent()) {
			previous.get().setNext(of(key));
		}

		// if key is the first key then return key as the new first
		Key<T> result;
		if (!previous.isPresent())
			result = key;
		else
			result = first;

		// update previous and following keys to the newly added one
		if (previous.isPresent()) {
			previous.get().setRight(key.getLeft());
			previous.get().updateLinks();
		}
		if (next.isPresent()) {
			next.get().setLeft(key.getRight());
			next.get().updateLinks();
		}
		key.updateLinks();
		return result;
	}

	/**
	 * Returns the number of keys in this node.
	 * 
	 * @return
	 */
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
	private Optional<Node<T>> add(Key<T> key) {

		key.setNode(of(this));

		if (!first.isPresent()) {
			setFirst(Optional.of(key));
			return Optional.of(this);
		}

		first = of(add(first.get(), key));

		Optional<Node<T>> result = absent();
		int keyCount = countKeys();
		if (keyCount == degree) {
			// split
			if (isRoot()) {
				// creating new root
				parent = Optional.of(new Node<T>(degree));
				result = parent;
			}
			Optional<Node<T>> result2 = splitKeysEitherSideOfMedianIntoTwoChildrenOfParent(keyCount);

			if (result2.isPresent())
				result = result2;

		} else
			result = absent();

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

	/**
	 * Returns the median key with the keys before it as left child and keys
	 * after it as right child.
	 * 
	 * @param keyCount
	 * @return
	 */
	private Optional<Node<T>> splitKeysEitherSideOfMedianIntoTwoChildrenOfParent(
			int keyCount) {
		int medianNumber = getMedianNumber(keyCount);

		// create child1
		Optional<Key<T>> key = first;
		int count = 1;
		Node<T> child1 = new Node<T>(degree, parent);
		child1.setFirst(first);

		Optional<Key<T>> previous = absent();
		while (count < medianNumber) {
			previous = key;
			key = key.get().next();
			count++;
		}
		Key<T> medianKey = key.get();
		previous.get().setNext(Optional.<Key<T>> absent());

		Node<T> child2 = new Node<T>(degree, parent);
		child2.setFirst(key.get().next());

		medianKey.setNext(Optional.<Key<T>> absent());
		medianKey.setLeft(Optional.of(child1));
		medianKey.setRight(Optional.of(child2));

		child1.updateParents();
		child2.updateParents();
		Optional<Node<T>> result = parent.get().add(medianKey);
		return result;
	}

	private void updateParents() {
		Optional<Key<T>> key = first;
		while (key.isPresent()) {
			key.get().setNode(of(this));
			key = key.get().next();
		}
	}

	/**
	 * Returns the median number between 1 and number of keys.
	 * 
	 * @param keyCount
	 * @return
	 */
	private int getMedianNumber(int keyCount) {
		int medianNumber;
		if (keyCount % 2 == 1)
			medianNumber = keyCount / 2 + 1;
		else
			medianNumber = (keyCount - 1) / 2 + 1;
		return medianNumber;
	}

	/**
	 * Returns the T matching t from this node or its children. Returns
	 * {@link Optional}.absent() if not found.
	 * 
	 * @param t
	 * @return
	 */
	public Optional<T> find(T t) {
		boolean isLeaf = isLeafNode();
		Optional<Key<T>> key = first;
		Optional<Key<T>> last = first;
		while (key.isPresent()) {
			int compare = t.compareTo(key.get().value());
			if (compare < 0) {
				if (isLeaf)
					return absent();
				else
					return key.get().getLeft().get().find(t);
			} else if (compare == 0 && !key.get().isDeleted())
				return Optional.of(key.get().value());
			last = key;
			key = key.get().next();
		}
		if (!isLeaf) {
			Optional<Node<T>> right = last.get().getRight();
			if (right.isPresent())
				return right.get().find(t);
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
		int count = 0;
		boolean isLeaf = isLeafNode();
		Optional<Key<T>> key = first;
		Optional<Key<T>> last = first;
		while (key.isPresent()) {
			int compare = t.compareTo(key.get().value());
			if (compare < 0) {
				if (isLeaf)
					return 0;
				else
					return key.get().getLeft().get().delete(t);
			} else if (compare == 0 && !key.get().isDeleted()) {
				count++;
				key.get().setDeleted(true);
			}
			last = key;
			key = key.get().next();
		}
		if (count > 0)
			return count;
		if (!isLeaf && last.isPresent()) {
			Optional<Node<T>> right = last.get().getRight();
			if (right.isPresent())
				return right.get().delete(t);
			else
				return 0;
		} else
			return 0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Node [");
		if (first.isPresent()) {
			builder.append("first=");
			builder.append(first.get());
		}
		if (parentKeySide.isPresent())
			builder.append(", pks=" + parentKeySide.get());
		builder.append("]");
		return builder.toString();
	}

	public String toString(String space) {
		StringBuilder builder = new StringBuilder();

		builder.append("\n" + space + "Node [");
		if (first.isPresent()) {
			builder.append("\n" + space + "  first=");
			builder.append(first.get().toString(space + "    "));
		}
		if (parentKeySide.isPresent())
			builder.append("\n" + space + "  pks=" + parentKeySide.get());
		builder.append("]");
		return builder.toString();
	}

	@VisibleForTesting
	List<? extends Key<T>> getKeys() {
		List<Key<T>> list = Lists.newArrayList();
		Optional<Key<T>> key = first;
		while (key.isPresent()) {
			list.add(key.get());
			key = key.get().next();
		}
		return list;
	}

	public void setFirst(Optional<Key<T>> first) {
		this.first = first;
		updateParents();
	}

	public void setParentKeySide(Optional<KeyAndSide<T>> parentKeySide) {
		this.parentKeySide = parentKeySide;
	}

	private Optional<Key<T>> bottomLeft() {
		if (isLeafNode())
			return this.first;
		else
			return first.get().getLeft().get().bottomLeft();
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			private Optional<Key<T>> currentKey = bottomLeft();

			@Override
			public boolean hasNext() {
				return currentKey.isPresent();
			}

			@Override
			public T next() {
				Preconditions.checkArgument(currentKey.isPresent(),
						"no more elements in iterator");
				T value = currentKey.get().value();
				currentKey = next(currentKey, false);
				return value;
			}

			private Optional<Key<T>> next(Optional<Key<T>> currentKey,
					boolean skipRight) {

				// move to bottom left of right child of current key if exists
				if (currentKey.get().getRight().isPresent() && !skipRight) {
					return currentKey.get().getRight().get().bottomLeft();
				} else if (currentKey.get().getRight().isPresent() && skipRight) {
					if (currentKey.get().next().isPresent())
						return currentKey.get().next();
					else {
						return nextParentKey(currentKey);
					}
				}
				// else to bottom left of next key if exists
				else if (currentKey.get().next().isPresent()) {
					Key<T> key = currentKey.get().next().get();
					if (key.hasChild())
						return key.getLeft().get().bottomLeft();
					else
						return of(key);
				}
				// else to next parent key if exists skipping right child
				else {
					return nextParentKey(currentKey);
				}
			}

			private Optional<Key<T>> nextParentKey(Optional<Key<T>> currentKey) {
				if (!currentKey.get().getParent().get().parentKeySide
						.isPresent())
					return absent();
				else {
					KeyAndSide<T> pkSide = currentKey.get().getParent().get().parentKeySide
							.get();
					if (pkSide.getSide().equals(Side.RIGHT)) {
						return next(of(pkSide.getKey()), true);
					} else
						return of(pkSide.getKey());
				}
			}

			@Override
			public void remove() {
				// TODO Auto-generated method stub

			}

		};
	}

	public String keysAsString() {
		StringBuilder s = new StringBuilder();
		Optional<Key<T>> key = first;
		while (key.isPresent()) {
			if (s.length() > 0)
				s.append(",");
			s.append(key.get().value());
			key = key.get().next();
		}
		return s.toString();
	}
}
