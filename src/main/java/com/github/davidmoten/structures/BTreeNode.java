package com.github.davidmoten.structures;

import java.util.ArrayList;
import java.util.List;

public class BTreeNode<T extends Comparable<T>> {

	private final List<BTreeKey<T>> keys;
	private final int degree;
	private BTreeNode<T> parent;

	public BTreeNode(int degree, BTreeNode<T> parent) {
		this.degree = degree;
		this.parent = parent;
		keys = new ArrayList<BTreeKey<T>>();
	}

	public BTreeNode(int degree) {
		this(degree, null);
	}

	public List<? extends BTreeKey<T>> getKeys() {
		return keys;
	}

	public BTreeNode<T> add(T t) {
		boolean added = false;
		for (int i = 0; i < keys.size(); i++) {
			BTreeKey<T> key = keys.get(i);
			if (t.compareTo(key.value()) < 0) {
				keys.add(i, new BTreeKey<T>(t));
				added = true;
			}
		}
		if (!added)
			keys.add(new BTreeKey<T>(t));
		if (keys.size() == degree) {
			// split
			if (parent == null) {
				parent = new BTreeNode<T>(degree, null);
				int medianIndex;
				if (keys.size() % 2 == 1)
					medianIndex = keys.size() / 2;
				else
					medianIndex = (keys.size() - 1) / 2;
				BTreeKey<T> medianKey = keys.get(medianIndex);
				parent.keys.add(medianKey);

				BTreeNode<T> child1 = new BTreeNode<T>(degree, parent);
				for (int i = 0; i < medianIndex; i++) {
					child1.keys.add(keys.get(i));
					if (i == medianIndex - 1)
						keys.get(i).setRight(null);
				}
				BTreeNode<T> child2 = new BTreeNode<T>(degree, parent);
				for (int i = medianIndex + 1; i < keys.size(); i++) {
					child2.keys.add(keys.get(i));
					if (i == medianIndex + 1)
						keys.get(i).setLeft(null);
				}

				medianKey.setLeft(child1);
				medianKey.setRight(child2);
				return parent;
			}
		} else
			return this;
	}
}
