package com.github.davidmoten.structures.btree;

import java.util.List;

public class BTree<T extends Comparable<T>> {

    private Node<T> root;

    public BTree(int degree) {
        root = new Node<T>(degree);
    }

    public void add(T key) {
        Node<T> newRoot = root.add(key);
        if (newRoot != null)
            root = newRoot;
    }

    public List<? extends Key<T>> getKeys() {
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
