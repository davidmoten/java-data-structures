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

    /**
     * Adds the element t to the node. If root node of BTree is changed then
     * returns new root node otherwise returns this.
     * 
     * @param t
     * @return
     */
    public BTreeNode<T> add(T t) {

        if (!isLeafNode()) {
            BTreeNode<T> result = null;
            for (BTreeKey<T> key : keys) {
                if (t.compareTo(key.value()) < 0) {
                    if (key.getLeft() == null)
                        key.setLeft(new BTreeNode<T>(degree, this));
                    result = key.getLeft().add(t);
                }
            }
            if (result != null) {
                BTreeKey<T> last = keys.get(keys.size() - 1);
                if (last.getRight() == null)
                    last.setRight(new BTreeNode<T>(degree, this));
                result = last.getRight().add(t);
            }
            return result;
        } else
            return add(new BTreeKey<T>(t));
    }

    private boolean isLeafNode() {
        for (BTreeKey<T> key : keys)
            if (key.hasChild())
                return false;
        return true;
    }

    /**
     * Inserts key into the list of keys in sorted order. The inserted key has
     * priority in terms of its children become the children of its neighbours
     * in the list of keys.
     * 
     * @param keys
     * @param key
     */
    private void add(List<BTreeKey<T>> keys, BTreeKey<T> key) {
        Integer addedAtIndex = null;

        for (int i = 0; i < keys.size(); i++) {
            BTreeKey<T> k = keys.get(i);
            if (key.compareTo(k) < 0) {
                keys.add(i, key);
                addedAtIndex = i;
            }
        }

        if (addedAtIndex == null) {
            keys.add(key);
            addedAtIndex = keys.size() - 1;
        }

        // update previous and following keys to the newly added one
        if (addedAtIndex > 0) {
            keys.get(addedAtIndex - 1).setRight(key.getLeft());
        }
        if (addedAtIndex < keys.size() - 1) {
            keys.get(addedAtIndex + 1).setLeft(key.getRight());
        }
    }

    /**
     * Adds the key to the node. If root node of BTree is changed then returns
     * new root node otherwise returns this.
     * 
     * @param key
     * @return
     */
    private BTreeNode<T> add(BTreeKey<T> key) {

        add(keys, key);

        final BTreeNode<T> result;
        if (keys.size() == degree) {
            // split
            if (parent == null) {
                parent = new BTreeNode<T>(degree, null);
                result = parent;
            } else
                result = this;

            int medianIndex = getMedianKeyIndex();

            BTreeKey<T> medianKey = keys.get(medianIndex);

            splitKeysEitherSideOfMedianIntoTwoChildrenOfParent(medianIndex);

            parent.add(medianKey);

        } else
            result = this;

        return result;

    }

    private void splitKeysEitherSideOfMedianIntoTwoChildrenOfParent(
            int medianIndex) {
        BTreeKey<T> medianKey = keys.get(medianIndex);
        BTreeNode<T> child1 = new BTreeNode<T>(degree, parent);
        for (int i = 0; i < medianIndex; i++) {
            child1.keys.add(keys.get(i));
        }
        medianKey.setLeft(child1);

        BTreeNode<T> child2 = new BTreeNode<T>(degree, parent);
        for (int i = medianIndex + 1; i < keys.size(); i++) {
            child2.keys.add(keys.get(i));
        }
        medianKey.setRight(child2);
    }

    private int getMedianKeyIndex() {
        int medianIndex;
        if (keys.size() % 2 == 1)
            medianIndex = keys.size() / 2;
        else
            medianIndex = (keys.size() - 1) / 2;
        return medianIndex;
    }

    public List<? extends BTreeKey<T>> getKeys() {
        return keys;
    }

}
