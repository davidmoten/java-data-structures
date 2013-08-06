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
import java.util.LinkedList;
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
class NodeActual<T extends Serializable & Comparable<T>> implements Iterable<T> {

	private Optional<Key<T>> first = Optional.absent();
	private final NodeListener<T> nodeListener;

	private final NodeRef<T> ref;
	private final int degree;

	/**
	 * Constructor.
	 * 
	 * @param position
	 * 
	 * @param degree
	 * @param parent
	 */
	NodeActual(NodeListener<T> nodeListener, NodeRef<T> ref, int degree) {
		this.nodeListener = nodeListener;
		this.ref = ref;
		this.degree = degree;
	}

	public AddResult<T> add(T t) {
		AddResult<T> result;
		if (isLeafNode()) {
			result = copy().add(new Key<T>(t));
		} else
			result = copy().addToNonLeafNode(t);
		if (result.getNode().isPresent()) {
			nodeListener.addToSaveQueue(result.getNode().get());
		}
		return result;
	}

	public KeyNodes<T> add(KeyNodes<T> keyNodes) {
		Preconditions.checkArgument(keyNodes.getKey().isPresent(),
				"key must be present");
		KeyNodes<T> result;
		if (isLeafNode())
			result = addToLeafNode(keyNodes);
		else
			result = addToNonLeafNode(keyNodes);
		return result;
	}

	private KeyNodes<T> addToLeafNode(KeyNodes<T> keyNodes) {

		NodeRef<T> node = insert(keyNodes.getKey().get());

		if (node.countKeys() == degree) {
			return node.split(keyNodes);
		} else
			return keyNodes.add(node);
	}

	NodeRef<T> insert(Key<T> key) {
		NodeRef<T> node = copy();
		node.insertHere(key);
		return node;
	}

	// TODO unit test this
	void insertHere(Key<T> key) {
		if (!first.isPresent())
			first = of(key.node(ref));
		else {
			Optional<Key<T>> k = first;
			Optional<Key<T>> previous = absent();
			boolean added = false;
			while (k.isPresent()) {
				if (key.value().compareTo(k.get().value()) < 0) {
					Key<T> newKey = key.nodeNext(ref, k);
					if (k == first)
						first = of(newKey);
					if (previous.isPresent()) {
						previous.get().setNext(of(newKey));
					}
					added = true;
					break;
				}
				previous = k;
				k = k.get().next();
			}

			if (!added) {
				// add as last key

				// k must be present because first was present
				previous.get().setNext(of(key.node(ref)));
			}
		}
	}

	private KeyNodes<T> addToNonLeafNode(KeyNodes<T> keyNodes) {

		Preconditions.checkArgument(keyNodes.getKey().isPresent(),
				"key must be present");
		Preconditions.checkArgument(first.isPresent(), "first must be present");
		// Note that first will be present because if is internal (non-leaf)
		// node then it must have some keys
		KeyNodes<T> result = null;
		boolean added = false;
		Optional<Key<T>> last = first;
		T t = keyNodes.getKey().get().value();
		for (Key<T> key : keys()) {
			if (t.compareTo(key.value()) < 0) {
				// don't need to check that left is present because of
				// properties of b-tree non-leaf node
				Preconditions.checkArgument(key.getLeft().isPresent(),
						"left must be present on non-leaf node " + key);
				final KeyNodes<T> addToLeftResult = key.getLeft().get()
						.add(keyNodes);
				result = processAddToChildResult(key, Side.LEFT,
						addToLeftResult);
				added = true;
				break;
			}
			last = of(key);
		}

		if (!added) {
			// don't need to check that left is present because of properties
			// of b-tree non leaf node
			final KeyNodes<T> addToRightResult = last.get().getRight().get()
					.add(keyNodes);
			result = processAddToChildResult(last.get(), Side.RIGHT,
					addToRightResult);
		}
		Preconditions.checkNotNull(result);
		return result;

	}

	private KeyNodes<T> processAddToChildResult(Key<T> key, Side side,
			final KeyNodes<T> addResult) {
		KeyNodes<T> result;
		if (addResult.getKey().isPresent()) {
			// add a split key to this node that came from key on side
			result = clearChild(key, side).add(addResult);
		} else {
			// create a new node based on this with key changed to point
			// to the last node on the list
			NodeRef<T> lastNodeAddedToSaveQueue = addResult.getSaveQueue()
					.peek();
			NodeRef<T> node = replace(key, side, lastNodeAddedToSaveQueue);
			// The key has definitely been added to node so put it on the
			// saveQueue
			result = addResult.add(node);
		}
		return result;
	}

	private NodeRef<T> clearChild(Key<T> key, Side side) {
		int i = indexOf(key).get();
		NodeRef<T> node = copy();
		node.key(i).setSide(side, Optional.<NodeRef<T>> absent());
		return node;
	}

	private Optional<Integer> indexOf(Key<T> key) {
		Optional<Key<T>> k = first;
		int index = 0;
		while (k.isPresent()) {
			if (k.get() == key)
				return of(index);
			index++;
			k = k.get().next();
		}
		return absent();
	}

	Key<T> key(int index) {
		Optional<Key<T>> k = first;
		for (int i = 0; i < index; i++)
			k = k.get().next();
		return k.get();
	}

	private NodeRef<T> replace(Key<T> key, Side side,
			NodeRef<T> lastNodeAddedToSaveQueue) {
		int i = indexOf(key).get();
		NodeRef<T> node = copy();
		node.replaceKeySide(i, side, lastNodeAddedToSaveQueue);
		return node;
	}

	void replaceKeySide(int keyIndex, Side side, NodeRef<T> replaceWith) {
		Key<T> k = key(keyIndex);
		k.setSide(side, of(replaceWith));
	}

	private NodeRef<T> copy() {
		NodeRef<T> node = new NodeRef<T>(nodeListener,
				Optional.<Long> absent(), degree);
		node.setFirst(copy(first));
		return node;
	}

	public AddResult<T> addToNonLeafNode(T t) {

		// Note that first will be present because if is internal (non-leaf)
		// node then it must have some keys
		AddResult<T> result = null;
		boolean added = false;
		Optional<Key<T>> last = first;
		for (Key<T> key : keys()) {
			if (t.compareTo(key.value()) < 0) {
				// don't need to check that left is present because of
				// properties of b-tree
				result = key.getLeft().get().add(t);
				if (result.getSplitKey().isPresent()) {
					// add a split key to this node
					result = add(result.getSplitKey().get());
				} else {
					key.setLeft(result.getNode());
					result = AddResult.createFromNonSplitNode(ref,
							result.getSaveQueue());
				}
				added = true;
				break;
			}
			last = of(key);
		}

		if (!added) {
			// don't need to check that left is present because of properties
			// of b-tree
			result = last.get().getRight().get().add(t);
			if (result.getSplitKey().isPresent()) {
				result = add(result.getSplitKey().get());
			} else {
				last.get().setRight(result.getNode());
				result = AddResult.createFromNonSplitNode(ref,
						result.getSaveQueue());
			}
		}
		Preconditions.checkNotNull(result);
		return result;
	}

	/**
	 * 
	 * Adds the given key to the current node. If this node needs to be split
	 * then returns the new node that is the parent of the split keys. If the
	 * node does not need to be split then returns the new node.
	 * 
	 * @param key
	 * @return
	 */
	public AddResult<T> add(Key<T> key) {

		key.setNode(of(ref));

		first = of(add(first, key));

		int keyCount = countKeys();

		return performSplitIfRequired(keyCount);
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
	private Key<T> add(Optional<Key<T>> first, Key<T> key) {
		// key is not on the current node
		key.setNode(of(ref));

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

	private AddResult<T> performSplitIfRequired(int keyCount) {
		final AddResult<T> result;
		if (keyCount == degree) {
			Key<T> key = splitKeysEitherSideOfMedianIntoTwoChildrenOfParent(keyCount);
			result = createFromSplitKey(key, new LinkedList<NodeRef<T>>());
		} else {
			result = createFromNonSplitNode(ref, new LinkedList<NodeRef<T>>());
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
	private Key<T> splitKeysEitherSideOfMedianIntoTwoChildrenOfParent(
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
		NodeRef<T> child1 = new NodeRef<T>(nodeListener,
				Optional.<Long> absent(), degree);
		child1.setFirst(list);
		nodeListener.addToSaveQueue(child1);

		// create child2 of medianKey.next ->..->last
		// this child will request a new file position
		NodeRef<T> child2 = new NodeRef<T>(nodeListener,
				Optional.<Long> absent(), degree);
		child2.setFirst(key.get().next());
		nodeListener.addToSaveQueue(child2);

		// set the links on medianKey to the next key in the same node and to
		// its children
		medianKey.setNext(Optional.<Key<T>> absent());
		medianKey.setLeft(Optional.of(child1));
		medianKey.setRight(Optional.of(child2));

		return medianKey;
	}

	KeyNodes<T> split(KeyNodes<T> keyNodes) {
		int medianNumber = getMedianNumber(countKeys());

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
		NodeRef<T> child1 = new NodeRef<T>(nodeListener,
				Optional.<Long> absent(), degree);
		child1.setFirst(list);

		// create child2 of medianKey.next ->..->last
		// this child will request a new file position
		NodeRef<T> child2 = new NodeRef<T>(nodeListener,
				Optional.<Long> absent(), degree);
		child2.setFirst(key.get().next());

		// set the links on medianKey to the next key in the same node and to
		// its children
		medianKey.setNext(Optional.<Key<T>> absent());
		medianKey.setLeft(Optional.of(child1));
		medianKey.setRight(Optional.of(child2));

		return keyNodes.add(child1).add(child2).key(medianKey);
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
	 * Returns the number of keys in this node.
	 * 
	 * @return
	 */
	int countKeys() {
		int count = 0;
		Optional<Key<T>> k = first;
		while (k.isPresent()) {
			count++;
			k = k.get().next();
		}
		return count;
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
			key.get().setNode(of(ref));
			key = key.get().next();
		}
	}

	/**
	 * Returns the median number between 1 and number of keys.
	 * 
	 * @param keyCount
	 * @return
	 */
	static int getMedianNumber(int keyCount) {
		Preconditions.checkArgument(keyCount >= 3);
		int medianNumber;
		if (keyCount % 2 == 1)
			medianNumber = keyCount / 2 + 1;
		else
			medianNumber = (keyCount - 1) / 2 + 1;
		return medianNumber;
	}

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
			Optional<NodeRef<T>> right = last.get().getRight();
			if (right.isPresent())
				return right.get().find(t);
			else
				return absent();
		} else
			return absent();
	}

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
			Optional<NodeRef<T>> right = last.get().getRight();
			if (right.isPresent())
				return right.get().delete(t);
			else
				return 0;
		} else
			return 0;
	}

	@VisibleForTesting
	public List<? extends Key<T>> getKeys() {
		List<Key<T>> list = Lists.newArrayList();
		for (Key<T> key : keys())
			list.add(key);
		return list;
	}

	public void setFirst(Optional<Key<T>> first) {
		Preconditions.checkNotNull(first);
		this.first = first;
		updateNode();
	}

	public Optional<Key<T>> getFirst() {
		return first;
	}

	@Override
	public Iterator<T> iterator() {
		return new NodeIterator<T>(ref);
	}

	public Iterable<Key<T>> keys() {
		return Util.keys(first);
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

	public void save(OutputStream os) {

		try {
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeInt(countKeys());
			for (Key<T> key : keys()) {
				oos.writeObject(key.value());
				if (key.getLeft().isPresent())
					oos.writeLong(key.getLeft().get().getPosition().get());
				else
					oos.writeLong(NodeRef.CHILD_ABSENT);
				if (key.getRight().isPresent())
					oos.writeLong(key.getRight().get().getPosition().get());
				else
					oos.writeLong(NodeRef.CHILD_ABSENT);
				oos.writeBoolean(key.isDeleted());
			}
			oos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	String abbr() {
		StringBuffer s = new StringBuffer();
		for (Key<T> key : keys()) {
			if (s.length() > 0)
				s.append(",");
			s.append(key.value());
		}
		return s.toString();
	}

	public void unload() {
		throw new RuntimeException("not expected here");
	}

}
