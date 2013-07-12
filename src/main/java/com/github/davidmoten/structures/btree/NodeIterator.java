package com.github.davidmoten.structures.btree;

import static com.google.common.base.Optional.of;

import java.io.Serializable;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import com.google.common.base.Optional;

class NodeIterator<T extends Serializable & Comparable<T>> implements
		Iterator<T> {

	private final Deque<KeySide<T>> q = new LinkedList<KeySide<T>>();

	NodeIterator(Node<T> node) {
		goToBottomLeft(of(node), q);
	}

	private void goToBottomLeft(Optional<Node<T>> node, Deque<KeySide<T>> q) {
		if (node.isPresent() && node.get().getFirst().isPresent()) {
			Key<T> first = node.get().getFirst().get();
			q.push(new KeySide<T>(first, Side.TOP));
			goToBottomLeft(node.get().getFirst().get().getLeft(), q);
		}
	}

	private Optional<KeySide<T>> next(KeySide<T> k) {
		if (k.getSide().equals(Side.LEFT))
			return of(new KeySide<T>(k.getKey(), Side.TOP));
		else if (k.getSide().equals(Side.TOP)) {
			if (k.getKey().getRight().isPresent())
				return of(new KeySide<T>(k.getKey(), Side.RIGHT));
			else
				return next(new KeySide<T>(k.getKey(), Side.RIGHT));
		} else {
			// side == Right
			if (k.getKey().next().isPresent())
				if (k.getKey().next().get().getLeft().isPresent())
					return of(new KeySide<T>(k.getKey().next().get(), Side.TOP));
				else
					return of(new KeySide<T>(k.getKey().next().get(), Side.TOP));
			else
				return Optional.absent();
		}
	}

	@Override
	public boolean hasNext() {
		return !q.isEmpty();
	}

	@Override
	public T next() {
		KeySide<T> key = q.pop();
		Optional<KeySide<T>> n = next(key);
		if (n.isPresent()) {
			if (n.get().getSide().equals(Side.TOP))
				q.push(n.get());
			else if (n.get().getSide().equals(Side.LEFT)) {
				Optional<KeySide<T>> n2 = next(n.get());
				if (n2.isPresent())
					q.push(n2.get());
				goToBottomLeft(n.get().getKey().getLeft(), q);
			} else if (n.get().getSide().equals(Side.RIGHT)) {
				Optional<KeySide<T>> n2 = next(n.get());
				if (n2.isPresent())
					q.push(n2.get());
				goToBottomLeft(n.get().getKey().getRight(), q);
			}
		}

		return key.getKey().value();
	}

	@Override
	public void remove() {
		throw new RuntimeException("not implemented");
	}

}
