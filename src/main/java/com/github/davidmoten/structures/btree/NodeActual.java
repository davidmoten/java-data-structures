package com.github.davidmoten.structures.btree;

import static com.github.davidmoten.structures.btree.AddResult.createFromNonSplitNode;
import static com.github.davidmoten.structures.btree.AddResult.createFromSplitKey;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * A leaf or non-leaf (internal) node on a B-Tree.
 * 
 * @author dxm
 * 
 * @param <T>
 */
class NodeActual<T extends Serializable & Comparable<T>> implements
		Iterable<T>, Node<T> {

	private Optional<Key<T>> first = Optional.absent();
	private final BTree<T> btree;

	private final long position;
	private final NodeRef<T> ref;

	/**
	 * Constructor.
	 * 
	 * @param position
	 * 
	 * @param degree
	 * @param parent
	 */
	NodeActual(BTree<T> btree, long position, NodeRef<T> ref) {
		this.btree = btree;
		this.position = position;
		this.ref = ref;
	}

	@Override
	public Optional<Node<T>> add(T t, ImmutableStack<Node<T>> stack) {
		if (isLeafNode()) {
			return add(new Key<T>(t), stack);
		} else
			return addToNonLeafNode(t, stack);
	}

	@Override
	public AddResult<T> add2(T t) {
		AddResult<T> result;
		if (isLeafNode()) {
			result = copy().add2(new Key<T>(t));
		} else
			result = copy().addToNonLeafNode2(t);
		btree.save(ref);
		return result;
	}

	private Node<T> copy() {
		NodeRef<T> node = new NodeRef<T>(btree, Optional.<Long> absent());
		node.setFirst(copy(first));
		return node;
	}

	@Override
	public AddResult<T> addToNonLeafNode2(T t) {

		// Note that first will be present because if is internal (non-leaf)
		// node then it must have some keys
		AddResult<T> result = null;
		boolean added = false;
		Optional<Key<T>> last = first;
		for (Key<T> key : keys()) {
			if (t.compareTo(key.value()) < 0) {
				// don't need to check that left is present because of
				// properties of b-tree
				result = key.getLeft().get().add2(t);
				if (result.getSplitKey().isPresent()) {
					// add a split key to this node
					result = add2(result.getSplitKey().get());
				} else {
					// TODO
				}
				added = true;
				break;
			}
			last = of(key);
		}

		if (!added) {
			// don't need to check that left is present because of properties
			// of b-tree
			result = last.get().getRight().get().add2(t);
			// TODO
		}
		return result;
	}

	/**
	 * 
	 * Adds the given key to the current node. If this node needs to be split
	 * then returns the new node that is the parent of the split keys. If the
	 * node does not need to be split then returns the new node.
	 * 
	 * @param key
	 * @param stack
	 * @return
	 */
	@Override
	public AddResult<T> add2(Key<T> key) {

		key.setNode(of((Node<T>) this));

		first = of(add2(first, key));

		int keyCount = countKeys();

		return performSplitIfRequired2(keyCount);
	}

	private Key<T> add2(Optional<Key<T>> first2, Key<T> key) {
		// TODO Auto-generated method stub
		return null;
	}

	private AddResult<T> performSplitIfRequired2(int keyCount) {
		final AddResult<T> result;
		if (keyCount == btree.getDegree())
			result = createFromSplitKey(splitKeysEitherSideOfMedianIntoTwoChildrenOfParent2(keyCount));
		else {
			result = createFromNonSplitNode(this);
		}
		return result;
	}

	/**
	 * Returns the median key with the keys before it as left child and keys
	 * after it as right child.
	 * 
	 * @param keyCount
	 * @param theParent
	 * @return
	 */
	private Key<T> splitKeysEitherSideOfMedianIntoTwoChildrenOfParent2(
			int keyCount) {

		int medianNumber = getMedianNumber(keyCount);

		// get the median key and the preceding key
		int count = 1;

		// for thread safety make a copy of the keys
		Optional<Key<T>> list = copy(first);

		Optional<Key<T>> key = list;
		Optional<Key<T>> previous = absent();
		while (count < medianNumber) {
			previous = key;
			key = key.get().next();
			count++;
		}
		Key<T> medianKey = key.get();

		previous.get().setNext(Optional.<Key<T>> absent());

		// create child1 of first ->..->previous
		// this child will request a new file position
		NodeRef<T> child1 = new NodeRef<T>(btree, Optional.<Long> absent());
		child1.setFirst(list);
		btree.save(child1);

		// create child2 of medianKey.next ->..->last
		// this child will request a new file position
		NodeRef<T> child2 = new NodeRef<T>(btree, Optional.<Long> absent());
		child2.setFirst(key.get().next());
		btree.save(child2);

		// set the links on medianKey to the next key in the same node and to
		// its children
		medianKey.setNext(Optional.<Key<T>> absent());
		medianKey.setLeft(Optional.<Node<T>> of(child1));
		medianKey.setRight(Optional.<Node<T>> of(child2));

		return medianKey;
	}

	/**
	 * Add
	 * 
	 * 
	 * e element to the node. If root node of BTree is changed then returns new
	 * root node otherwise returns {@link Optional}.absent().
	 * 
	 * @param t
	 * @return
	 */
	private Optional<Node<T>> addToNonLeafNode(T t,
			ImmutableStack<Node<T>> stack) {
		// Note that first will be present because if is internal (non-leaf)
		// node then it must have some keys
		Optional<Node<T>> result = absent();
		boolean added = false;
		Optional<Key<T>> last = first;
		for (Key<T> key : keys()) {
			if (t.compareTo(key.value()) < 0) {
				// don't need to check that left is present because of
				// properties of b-tree
				result = key.getLeft().get().add(t, stack.push(this));
				added = true;
				break;
			}
			last = of(key);
		}

		if (!added) {
			// don't need to check that left is present because of properties
			// of b-tree
			result = last.get().getRight().get().add(t, stack.push(this));
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
	 * @param key
	 */
	private Key<T> add(Optional<Key<T>> first, Key<T> key,
			ImmutableStack<Node<T>> stack) {

		// key is not on the current node
		key.setNode(of((Node<T>) this));

		// insert key in the list if before one
		Optional<Key<T>> previous = absent();
		Optional<Key<T>> next = absent();
		for (Key<T> k : Util.keys(first)) {
			if (key.value().compareTo(k.value()) < 0) {
				// it is important to set next before set previous so that
				// concurrent reads work correctly
				key.setNext(of(k));
				if (previous.isPresent())
					previous.get().setNext(of(key));
				next = of(k);
				break;
			}
			previous = of(k);
		}

		if (!next.isPresent() && previous.isPresent()) {
			previous.get().setNext(of(key));
		}

		// if key is the first key then return key as the new first
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

	/**
	 * Returns the number of keys in this node.
	 * 
	 * @return
	 */
	@Override
	public int countKeys() {
		int count = 0;
		Optional<Key<T>> k = first;
		while (k.isPresent()) {
			count++;
			k = k.get().next();
		}
		return count;
	}

	@Override
	public Optional<Node<T>> add(Key<T> key, ImmutableStack<Node<T>> stack) {

		key.setNode(of((Node<T>) this));

		first = of(add(first, key, stack));

		int keyCount = countKeys();

		return performSplitIfRequired(keyCount, stack);

	}

	private Optional<Node<T>> performSplitIfRequired(int keyCount,
			ImmutableStack<Node<T>> stack) {
		final Optional<Node<T>> result;
		if (keyCount == btree.getDegree())
			result = splitKeysEitherSideOfMedianIntoTwoChildrenOfParent(
					keyCount, stack);
		else {
			btree.save(ref);
			result = absent();
		}
		return result;
	}

	private Optional<Node<T>> splitKeysEitherSideOfMedianIntoTwoChildrenOfParent(
			int keyCount, ImmutableStack<Node<T>> stack) {
		final Optional<Node<T>> result;
		Node<T> theParent;
		Optional<Node<T>> result1;

		// split
		if (isRoot(stack)) {
			// creating new root
			theParent = new NodeRef<T>(btree, Optional.<Long> absent());
			result1 = of(theParent);
			stack = stack.push(theParent);
		} else {
			theParent = stack.peek().get();
			result1 = absent();
		}
		// split result is present if root changed by splitting
		Optional<Node<T>> splitResult = splitKeysEitherSideOfMedianIntoTwoChildrenOfParent(
				keyCount, theParent, stack);

		if (splitResult.isPresent())
			result = splitResult;
		else
			result = result1;

		return result;
	}

	/**
	 * Returns true if and only if this is the root node of the BTree (has no
	 * parent).
	 * 
	 * @return
	 */
	private boolean isRoot(ImmutableStack<Node<T>> stack) {
		return stack.isEmpty();
	}

	/**
	 * Returns the median key with the keys before it as left child and keys
	 * after it as right child.
	 * 
	 * @param keyCount
	 * @param theParent
	 * @return
	 */
	private Optional<Node<T>> splitKeysEitherSideOfMedianIntoTwoChildrenOfParent(
			int keyCount, Node<T> parent, ImmutableStack<Node<T>> stack) {

		int medianNumber = getMedianNumber(keyCount);

		// get the median key and the preceding key
		int count = 1;

		// for thread safety make a copy of the keys
		Optional<Key<T>> list = copy(first);

		Optional<Key<T>> key = list;
		Optional<Key<T>> previous = absent();
		while (count < medianNumber) {
			previous = key;
			key = key.get().next();
			count++;
		}
		Key<T> medianKey = key.get();

		previous.get().setNext(Optional.<Key<T>> absent());

		// create child1 of first ->..->previous
		// this child will request a new file position
		NodeRef<T> child1 = new NodeRef<T>(btree, Optional.<Long> absent());
		child1.setFirst(list);
		btree.save(child1);

		// create child2 of medianKey.next ->..->last
		// this child will request a new file position
		NodeRef<T> child2 = new NodeRef<T>(btree, Optional.<Long> absent());
		child2.setFirst(key.get().next());
		btree.save(child2);

		// set the links on medianKey to the next key in the same node and to
		// its children
		medianKey.setNext(Optional.<Key<T>> absent());
		medianKey.setLeft(Optional.<Node<T>> of(child1));
		medianKey.setRight(Optional.<Node<T>> of(child2));

		Optional<Node<T>> result = parent.add(medianKey, stack.pop());

		// mark the current node position for reuse
		btree.getPositionManager().releaseNodePosition(position);

		return result;
	}

	private Optional<Key<T>> copy(Optional<Key<T>> list) {
		Optional<Key<T>> result = absent();
		Optional<Key<T>> key = list;
		Optional<Key<T>> lastCreated = absent();
		while (key.isPresent()) {
			// copy the key
			Key<T> k = new Key<T>(key.get().value());
			k.setLeft(key.get().getLeft());
			k.setRight(key.get().getRight());
			k.setDeleted(key.get().isDeleted());
			// create first if does not exist
			if (!result.isPresent())
				result = of(k);
			// link to previous
			if (lastCreated.isPresent())
				lastCreated.get().setNext(of(k));
			lastCreated = of(k);
			key = key.get().next();
		}
		return result;
	}

	private void updateNode() {
		Optional<Key<T>> key = first;
		while (key.isPresent()) {
			key.get().setNode(of((Node<T>) this));
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

	@Override
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

	@Override
	public long delete(T t) {
		int count = 0;
		boolean isLeaf = isLeafNode();
		Optional<Key<T>> last = Optional.absent();
		for (Key<T> key : keys()) {
			int compare = t.compareTo(key.value());
			if (compare < 0) {
				if (isLeaf)
					return 0;
				else
					return key.getLeft().get().delete(t);
			} else if (compare == 0 && !key.isDeleted()) {
				count++;
				key.setDeleted(true);
			}
			last = of(key);
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
	@VisibleForTesting
	public List<? extends Key<T>> getKeys() {
		List<Key<T>> list = Lists.newArrayList();
		for (Key<T> key : keys())
			list.add(key);
		return list;
	}

	@Override
	public void setFirst(Optional<Key<T>> first) {
		Preconditions.checkNotNull(first);
		this.first = first;
		updateNode();
	}

	@Override
	public Optional<Key<T>> bottomLeft() {
		if (isLeafNode())
			return this.first;
		else
			return first.get().getLeft().get().bottomLeft();
	}

	@Override
	public Optional<Key<T>> getFirst() {
		return first;
	}

	@Override
	public Iterator<T> iterator() {
		return new NodeIterator<T>(this);
	}

	@Override
	public Iterable<Key<T>> keys() {
		return Util.keys(first);
	}

	@Override
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

	@Override
	public String toString(String space) {
		StringBuilder builder = new StringBuilder();

		builder.append("\n" + space + "Node [");
		if (first.isPresent()) {
			builder.append("\n" + space + "  first=");
			builder.append(first.get().toString(space + "    "));
		}
		builder.append("]");
		return builder.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Node [");
		if (first.isPresent()) {
			builder.append("first=");
			builder.append(first.get());
		}
		builder.append("]");
		return builder.toString();
	}

	@Override
	public void save(OutputStream os) {

		try {
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeInt(countKeys());
			for (Key<T> key : keys()) {
				oos.writeObject(key.value());
				if (key.getLeft().isPresent())
					oos.writeLong(key.getLeft().get().getPosition());
				else
					oos.writeLong(NodeRef.CHILD_ABSENT);
				if (key.getRight().isPresent())
					oos.writeLong(key.getRight().get().getPosition());
				else
					oos.writeLong(NodeRef.CHILD_ABSENT);
				oos.writeBoolean(key.isDeleted());
			}
			oos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// private String abbr() {
	// StringBuffer s = new StringBuffer();
	// for (Key<T> key : keys()) {
	// if (s.length() > 0)
	// s.append(",");
	// s.append(key.value());
	// }
	// return s.toString();
	// }

	@Override
	public long getPosition() {
		return position;
	}

	@Override
	public void unload() {
		throw new RuntimeException("not expected here");
	}

}
