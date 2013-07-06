package com.github.davidmoten.structures.btree;

import java.io.Serializable;

class KeySide<T extends Serializable & Comparable<T>> {
	private final Key<T> key;
	private final Side side;

	KeySide(Key<T> key, Side side) {
		this.key = key;
		this.side = side;
	}

	Key<T> getKey() {
		return key;
	}

	Side getSide() {
		return side;
	}

	@Override
	public String toString() {
		return "KeyAndSide [key=" + key.value() + ", side=" + side + "]";
	}

}
