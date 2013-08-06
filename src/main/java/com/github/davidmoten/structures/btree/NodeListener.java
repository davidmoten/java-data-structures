package com.github.davidmoten.structures.btree;

import java.io.Serializable;

public interface NodeListener<T extends Serializable & Comparable<T>> {

	void load(NodeRef<T> node);
}
