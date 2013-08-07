package com.github.davidmoten.structures.btree;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.CountingInputStream;

/**
 * A leaf or non-leaf (internal) node on a B-Tree.
 * 
 * @author dxm
 * 
 * @param <T>
 */
class Node<T extends Serializable & Comparable<T>> implements Iterable<T> {

	static final int CHILD_ABSENT = -1;
	private Optional<Key<T>> first = Optional.absent();
	private final NodeLoader<T> loader;

	private final NodeRef<T> ref;
	private final int degree;
	private boolean isRoot;

	/**
	 * Constructor.
	 * 
	 * @param position
	 * 
	 * @param degree
	 * @param parent
	 */
	Node(NodeLoader<T> nodeListener, NodeRef<T> ref, boolean isRoot) {
		this.loader = nodeListener;
		this.ref = ref;
		this.degree = ref.getDegree();
		this.isRoot = isRoot;
	}

	public KeyNodes<T> add(KeyNodes<T> keyNodes) {
		Preconditions.checkArgument(keyNodes.getKey().isPresent(),
				"key must be present");
		KeyNodes<T> result;
		if (isLeafNode())
			result = addToThisLevel(keyNodes);
		else
			result = addToNonLeafNode(keyNodes);
		return result;
	}

	KeyNodes<T> addToThisLevel(KeyNodes<T> keyNodes) {

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
			first = of(key);
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
						// key overrides the right child of previous
						previous.get().setRight(newKey.getLeft());
					}
					k.get().setLeft(newKey.getRight());
					added = true;
					break;
				}
				previous = k;
				k = k.get().next();
			}

			if (!added) {
				// add as last key

				// k must be present because first was present
				previous.get().setNext(of(key));
				// key overrides the right child of previous
				previous.get().setRight(key.getLeft());
			}
		}
	}

	private KeyNodes<T> addToNonLeafNode(KeyNodes<T> keyNodes) {

		Preconditions.checkArgument(keyNodes.getKey().isPresent(),
				"key must be present");
		Preconditions.checkArgument(first.isPresent(), "first must be present");
		// Note that first will be present because if is internal (non-leaf)
		// node then it must have some keys

		// using optional despite result always being present by the time this
		// method returns
		Optional<KeyNodes<T>> result = absent();
		boolean added = false;
		Optional<Key<T>> last = first;
		T t = keyNodes.getKey().get().value();
		for (Key<T> key : keys()) {
			if (t.compareTo(key.value()) < 0) {
				// don't need to check that left is present because of
				// properties of b-tree non-leaf node
				Preconditions.checkArgument(key.getLeft().isPresent(),
						"left must be present on non-leaf node");
				final KeyNodes<T> addToLeftResult = key.getLeft().get()
						.add(keyNodes);
				result = of(processAddToChildResult(key, Side.LEFT,
						addToLeftResult));
				added = true;
				break;
			}
			last = of(key);
		}

		if (!added) {
			// don't need to check that right is present because of properties
			// of b-tree non leaf node
			final KeyNodes<T> addToRightResult = last.get().getRight().get()
					.add(keyNodes);
			result = of(processAddToChildResult(last.get(), Side.RIGHT,
					addToRightResult));
		}
		Preconditions.checkNotNull(result);
		// result should always be present
		return result.get();

	}

	private KeyNodes<T> processAddToChildResult(Key<T> key, Side side,
			final KeyNodes<T> addResult) {
		KeyNodes<T> result;
		if (addResult.getKey().isPresent()) {
			// add a split key to this node that came from key on side
			result = clearChild(key, side).addToThisLevel(addResult);
		} else {
			// create a new node based on this with key changed to point
			// to the last node on the list
			NodeRef<T> lastNodeAddedToSaveQueue = addResult.getSaveQueue()
					.getLast();
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
		node.key(i).clear(side);
		if (side.equals(Side.LEFT) && i > 0)
			node.key(i - 1).clear(Side.RIGHT);
		else if (side.equals(Side.RIGHT) && i < countKeys() - 1)
			node.key(i + 1).clear(Side.LEFT);
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
		NodeRef<T> node = new NodeRef<T>(loader, Optional.<Position> absent(),
				degree, isRoot);
		node.setFirst(copy(first));
		return node;
	}

	KeyNodes<T> split(KeyNodes<T> keyNodes) {
		return copy().splitHere(keyNodes);
	}

	/**
	 * @param keyNodes
	 * @return
	 */
	KeyNodes<T> splitHere(KeyNodes<T> keyNodes) {
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
		NodeRef<T> child1 = new NodeRef<T>(loader,
				Optional.<Position> absent(), degree, false);
		child1.setFirst(list);

		// create child2 of medianKey.next ->..->last
		// this child will request a new file position
		NodeRef<T> child2 = new NodeRef<T>(loader,
				Optional.<Position> absent(), degree, false);
		child2.setFirst(key.get().next());

		// set the links on medianKey to the next key in the same node and to
		// its children
		medianKey.setNext(Optional.<Key<T>> absent());
		medianKey.setLeft(Optional.of(child1));
		medianKey.setRight(Optional.of(child2));

		first = of(medianKey);
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

	public Iterable<T> findAll(T t) {
		throw new RuntimeException("not implemented");
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

	long load(InputStream is) {
		try {
			CountingInputStream cis = new CountingInputStream(is);
			@SuppressWarnings("resource")
			ObjectInputStream ois = new ObjectInputStream(cis);
			isRoot = ois.readBoolean();
			// used for can delete for space recovery by LSS
			ois.readBoolean();
			int count = ois.readInt();
			Optional<Key<T>> previous = absent();
			Optional<Key<T>> firstKey = absent();
			for (int i = 0; i < count; i++) {
				@SuppressWarnings("unchecked")
				T t = (T) ois.readObject();
				long leftFileNumber = ois.readLong();
				long left = ois.readLong();
				long rightFileNumber = ois.readLong();
				long right = ois.readLong();
				boolean deleted = ois.readBoolean();
				Key<T> key = new Key<T>(t);
				if (left != CHILD_ABSENT)
					key.setLeft(of(new NodeRef<T>(loader, of(new Position(
							leftFileNumber, left)), degree, false)));
				if (right != CHILD_ABSENT)
					key.setRight(of(new NodeRef<T>(loader, of(new Position(
							rightFileNumber, right)), degree, false)));
				key.setDeleted(deleted);
				key.setNext(Optional.<Key<T>> absent());
				if (!firstKey.isPresent())
					firstKey = of(key);
				if (previous.isPresent())
					previous.get().setNext(of(key));
				previous = of(key);
			}

			// don't close the input stream to avoid closing the underlying
			// stream
			first = firstKey;
			return cis.getCount();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void save(OutputStream os) {

		try {
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeBoolean(isRoot);
			oos.writeBoolean(false);
			oos.writeInt(countKeys());
			for (Key<T> key : keys()) {
				oos.writeObject(key.value());
				if (key.getLeft().isPresent()) {
					oos.writeLong(key.getLeft().get().getPosition().get()
							.getFileNumber());
					oos.writeLong(key.getLeft().get().getPosition().get()
							.getPosition());
				} else {
					oos.writeLong(CHILD_ABSENT);
					oos.writeLong(CHILD_ABSENT);
				}
				if (key.getRight().isPresent()) {
					oos.writeLong(key.getRight().get().getPosition().get()
							.getFileNumber());
					oos.writeLong(key.getRight().get().getPosition().get()
							.getPosition());
				} else {
					oos.writeLong(CHILD_ABSENT);
					oos.writeLong(CHILD_ABSENT);
				}
				oos.writeBoolean(key.isDeleted());
			}
			oos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	String abbr2() {
		StringBuffer s = new StringBuffer();
		for (Key<T> key : keys()) {
			if (s.length() > 0)
				s.append(",");
			s.append(key.value());
		}
		return s.toString();
	}

	String abbr() {
		StringBuffer s = new StringBuffer();
		for (Key<T> key : keys()) {
			if (s.length() > 0)
				s.append(",");
			s.append(abbr(key));
		}
		return s.toString();
	}

	private String abbr(Key<T> key) {
		StringBuffer s = new StringBuffer();
		s.append(key.value());
		if (key.getLeft().isPresent()) {
			s.append("L[");
			s.append(key.getLeft().get().abbr());
			s.append("]");
		}
		if (key.getRight().isPresent()) {
			s.append("R[");
			s.append(key.getRight().get().abbr());
			s.append("]");
		}
		return s.toString();
	}

	public void setIsRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	public boolean isRoot() {
		return isRoot;
	}

}
