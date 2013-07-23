package com.github.davidmoten.structures.btree;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Optional;

public class NodeRef<T extends Serializable & Comparable<T>> implements Node<T> {

	static final int CHILD_ABSENT = -1;

	private Optional<Long> position;

	private Optional<NodeActual<T>> node = Optional.absent();
	private final BTree<T> btree;

	public NodeRef(BTree<T> btree, Optional<Long> position) {
		this.btree = btree;
		this.position = position;
	}

	private synchronized Node<T> node() {
		if (!node.isPresent()) {
			if (position.isPresent()) {
				load();
			} else {
				position = of(btree.getPositionManager().nextPosition());
				node = of(new NodeActual<T>(btree, position.get(), this));
			}
		}
		return node.get();
	}

	void load(InputStream is) {

		try {
			ObjectInputStream ois = new ObjectInputStream(is);
			int count = ois.readInt();
			Optional<Key<T>> previous = absent();
			Optional<Key<T>> first = absent();
			for (int i = 0; i < count; i++) {
				@SuppressWarnings("unchecked")
				T t = (T) ois.readObject();
				long left = ois.readLong();
				long right = ois.readLong();
				boolean deleted = ois.readBoolean();
				Key<T> key = new Key<T>(t);
				if (left != CHILD_ABSENT)
					key.setLeft(of((Node<T>) new NodeRef<T>(btree, of(left))));
				if (right != CHILD_ABSENT)
					key.setRight(of((Node<T>) new NodeRef<T>(btree, of(right))));
				key.setDeleted(deleted);
				key.setNode(of((Node<T>) this));
				key.setNext(Optional.<Key<T>> absent());
				if (!first.isPresent())
					first = of(key);
				if (previous.isPresent())
					previous.get().setNext(of(key));
				previous = of(key);
			}
			node.get().setFirst(first);
			ois.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private void load() {

		node = of(new NodeActual<T>(btree, position.get(), this));
		btree.load(this);
	}

	@Override
	public Optional<Node<T>> add(T t, ImmutableStack<Node<T>> stack) {
		return node().add(t, stack);
	}

	@Override
	public Optional<Node<T>> add(Key<T> key, ImmutableStack<Node<T>> stack) {
		return node().add(key, stack);
	}

	@Override
	public Optional<T> find(T t) {
		return node().find(t);
	}

	@Override
	public long delete(T t) {
		return node().delete(t);
	}

	@Override
	public List<? extends Key<T>> getKeys() {
		return node().getKeys();
	}

	@Override
	public void setFirst(Optional<Key<T>> first) {
		node().setFirst(first);
	}

	@Override
	public Optional<Key<T>> getFirst() {
		return node().getFirst();
	}

	@Override
	public Optional<Key<T>> bottomLeft() {
		return node().bottomLeft();
	}

	@Override
	public Iterator<T> iterator() {
		return node().iterator();
	}

	@Override
	public String keysAsString() {
		return node().keysAsString();
	}

	@Override
	public String toString(String space) {
		return node().toString(space);
	}

	@Override
	public String toString() {
		if (node.isPresent()) {
			return node.toString();
		} else
			return asString();
	}

	public String asString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NodeRef [position=");
		builder.append(position);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public long getPosition() {
		return node().getPosition();
	}

	@Override
	public void unload() {
		node = absent();
	}

	@Override
	public AddResult<T> add2(T t) {
		return node().add2(t);
	}

	@Override
	public AddResult<T> addToNonLeafNode2(T t) {
		return node().addToNonLeafNode2(t);
	}

	@Override
	public AddResult<T> add2(Key<T> key) {
		return node().add2(key);
	}


	@Override
	public Iterable<Key<T>> keys() {
		return node().keys();
	}

	@Override
	public void save(OutputStream os) {
		node().save(os);

	}

}
