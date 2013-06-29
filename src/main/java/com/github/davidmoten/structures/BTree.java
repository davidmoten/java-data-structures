package com.github.davidmoten.structures;

import java.util.List;

public class BTree<T extends Comparable<T>> {

	private BTreeNode<T> root;

	public BTree(int degree) {
		root = new BTreeNode<T>(degree);
	}

	public void add(T key) {
		root = root.add(key);
	}

	public List<? extends BTreeKey<T>> getKeys() {
		return root.getKeys();
	}
}
