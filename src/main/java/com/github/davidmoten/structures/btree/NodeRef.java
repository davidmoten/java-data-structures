package com.github.davidmoten.structures.btree;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Optional;

public class NodeRef<T extends Serializable & Comparable<T>> {

	private Optional<Position> position;

	private Optional<Node<T>> node = Optional.absent();
	private final NodeLoader<T> loader;

	private final int degree;

	private final boolean isRoot;

	public NodeRef(NodeLoader<T> nodeListener, Optional<Position> position,
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

	public Optional<T> find(T t) {
		return node().find(t);
	}

	public Iterable<T> findAll(T t) {
		return node().findAll(t);
	}

	public long delete(T t) {
		return node().delete(t);
	}

	public List<? extends Key<T>> getKeys() {
		return node().getKeys();
	}

	public void setFirst(Optional<Key<T>> first) {
		node().setFirst(first);
	}

	public Optional<Key<T>> getFirst() {
		return node().getFirst();
	}

	public Iterator<T> iterator() {
		return node().iterator();
	}

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

	public Optional<Position> getPosition() {
		return position;
	}

	public void unload() {
		// System.out.println("unloaded " + position);
		node = absent();
	}

	public KeyNodes<T> add(KeyNodes<T> keyNodes) {
		return node().add(keyNodes);
	}

	void replaceKeySide(int keyIndex, Side side,
			NodeRef<T> lastNodeAddedToSaveQueue) {
		node().replaceKeySide(keyIndex, side, lastNodeAddedToSaveQueue);
	}

	KeyNodes<T> addToThisLevel(KeyNodes<T> keyNodes) {
		return node().addToThisLevel(keyNodes);
	}

	public Iterable<Key<T>> keys() {
		return node().keys();
	}

	public void save(OutputStream os) {
		node().save(os);
	}

	public void setPosition(Optional<Position> position) {
		this.position = position;
	}

	public int countKeys() {
		return node().countKeys();
	}

	public KeyNodes<T> split(KeyNodes<T> keyNodes) {
		return node().split(keyNodes);
	}

	KeyNodes<T> splitHere(KeyNodes<T> keyNodes) {
		return node().splitHere(keyNodes);
	}

	public void insertHere(Key<T> key) {
		node().insertHere(key);
	}

	public Key<T> key(int i) {
		return node().key(i);
	}

	public String abbr() {
		return node().abbr();
	}

	public boolean isRoot() {
		return node().isRoot();
	}

	public int getDegree() {
		return degree;
	}
}
