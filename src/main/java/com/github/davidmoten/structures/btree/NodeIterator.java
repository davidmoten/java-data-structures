package com.github.davidmoten.structures.btree;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

import java.io.Serializable;
import java.util.Iterator;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

class NodeIterator<T extends Serializable & Comparable<T>> implements
		Iterator<T> {

	private Optional<Key<T>> currentKey;

	NodeIterator(Node<T> node) {
		currentKey = node.bottomLeft();
	}

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

	private Optional<Key<T>> next(Optional<Key<T>> currentKey, boolean skipRight) {

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
		if (!currentKey.get().getParent().get().getParentKeySide().isPresent())
			return absent();
		else {
			KeySide<T> pkSide = currentKey.get().getParent().get()
					.getParentKeySide().get();
			if (pkSide.getSide().equals(Side.RIGHT)) {
				return next(of(pkSide.getKey()), true);
			} else
				return of(pkSide.getKey());
		}
	}

	@Override
	public void remove() {
		throw new RuntimeException("not implemented");
	}

}
