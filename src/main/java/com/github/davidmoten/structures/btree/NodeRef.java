package com.github.davidmoten.structures.btree;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Optional;

class NodeRef<T extends Serializable & Comparable<T>> {

	private Optional<Position> position;

	private Optional<Node<T>> node = Optional.absent();
	private final NodeLoader<T> loader;

	private final int degree;

	private final boolean isRoot;

	NodeRef(NodeLoader<T> nodeListener, Optional<Position> position,
			int degree, boolean isRoot) {
		this.loader = nodeListener;
		this.position = position;
		this.degree = degree;
		this.isRoot = isRoot;
	}

	synchronized Node<T> node() {
		if (!node.isPresent()) {
			if (position.isPresent()) {
				load();
			} else {
				node = of(new Node<T>(loader, this, isRoot));
			}
		}
		return node.get();
	}

	void load(InputStream is) {
		node.get().load(is);
	}

	private void load() {
		node = of(new Node<T>(loader, this, isRoot));
		loader.load(this);
	}

	Optional<T> find(T t) {
		return node().find(t);
	}

	Iterable<T> findAll(T t) {
		return node().findAll(t);
	}

	long delete(T t) {
		return node().delete(t);
	}

	List<? extends Key<T>> getKeys() {
		return node().getKeys();
	}

	void setFirst(Optional<Key<T>> first) {
		node().setFirst(first);
	}

	Optional<Key<T>> getFirst() {
		return node().getFirst();
	}

	Iterator<T> iterator() {
		return node().iterator();
	}

	String toString(String space) {
		return node().toString(space);
	}

	@Override
	public String toString() {
		if (node.isPresent()) {
			return node.toString();
		} else
			return asString();
	}

	String asString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NodeRef [position=");
		builder.append(position);
		builder.append("]");
		return builder.toString();
	}

	Optional<Position> getPosition() {
		return position;
	}

	void unload() {
		// System.out.println("unloaded " + position);
		node = absent();
	}

	KeyNodes<T> add(KeyNodes<T> keyNodes) {
		return node().add(keyNodes);
	}

	void replaceKeySide(int keyIndex, Side side,
			NodeRef<T> lastNodeAddedToSaveQueue) {
		node().replaceKeySide(keyIndex, side, lastNodeAddedToSaveQueue);
	}

	KeyNodes<T> addToThisLevel(KeyNodes<T> keyNodes) {
		return node().addToThisLevel(keyNodes);
	}

	Iterable<Key<T>> keys() {
		return node().keys();
	}

	void save(OutputStream os) {
		node().save(os);
	}

	void setPosition(Optional<Position> position) {
		this.position = position;
	}

	int countKeys() {
		return node().countKeys();
	}

	KeyNodes<T> split(KeyNodes<T> keyNodes) {
		return node().split(keyNodes);
	}

	KeyNodes<T> splitHere(KeyNodes<T> keyNodes) {
		return node().splitHere(keyNodes);
	}

	void insertHere(Key<T> key) {
		node().insertHere(key);
	}

	Key<T> key(int i) {
		return node().key(i);
	}

	String abbr() {
		return node().abbr();
	}

	boolean isRoot() {
		return node().isRoot();
	}

	int getDegree() {
		return degree;
	}
}
