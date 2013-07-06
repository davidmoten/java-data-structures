package com.github.davidmoten.structures.btree;

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Optional;

public class NodeRef<T extends Comparable<T>> implements Node<T> {

	private Optional<Node<T>> node;

	private Node<T> node() {
		return node.get();
	}

	@Override
	public Optional<Node<T>> add(T t) {
		return node().add(t);
	}

	@Override
	public Optional<Node<T>> add(Key<T> key) {
		return node().add(key);
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
	public Optional<KeySide<T>> getParentKeySide() {
		return node().getParentKeySide();
	}

	@Override
	public void setParentKeySide(Optional<KeySide<T>> parentKeySide) {
		node().setParentKeySide(parentKeySide);
	}

	@Override
	public String toString(String space) {
		return node().toString(space);
	}

}
