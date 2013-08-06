package com.github.davidmoten.structures.btree;

import java.io.Serializable;

public interface NodeLoader<T extends Serializable & Comparable<T>> {

	void load(NodeRef<T> node);
}
