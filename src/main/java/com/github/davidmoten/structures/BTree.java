package com.github.davidmoten.structures;

import java.util.List;

public class BTree<T extends Comparable<T>> {

    private BTreeNode<T> root;

    public BTree(int degree) {
        root = new BTreeNode<T>(degree);
    }

    public void add(T key) {
        BTreeNode<T> newRoot = root.add(key);
        if (newRoot != null)
            root = newRoot;
    }

    public List<? extends BTreeKey<T>> getKeys() {
        return root.getKeys();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BTree [root=");
        builder.append(root);
        builder.append("]");
        return builder.toString();
    }

}
