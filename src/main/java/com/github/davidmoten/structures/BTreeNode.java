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
     * returns new root node otherwise returns null.
     * 
     * @param t
     * @return
     */
    public BTreeNode<T> add(T t) {

        if (!isLeafNode()) {
            return addToNonLeafNode(t);
        } else
            return add(new BTreeKey<T>(t));
    }

    private BTreeNode<T> addToNonLeafNode(T t) {
        BTreeNode<T> result = null;
        boolean added = false;
        for (int i = 0; i < keys.size(); i++) {
            BTreeKey<T> key = keys.get(i);
            if (t.compareTo(key.value()) < 0) {
                if (key.getLeft() == null)
                    key.setLeft(new BTreeNode<T>(degree, parent));
                result = key.getLeft().add(t);
                added = true;
                break;
            }
        }
        if (!added) {
            BTreeKey<T> last = keys.get(keys.size() - 1);
            if (last.getRight() == null)
                last.setRight(new BTreeNode<T>(degree, parent));
            result = last.getRight().add(t);
        }
        return result;
    }

    /**
     * Returns true if and only this node is a leaf node (has no children).
     * 
     * @return
     */
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
        System.out.println("adding " + key + " to " + keys);
        Integer addedAtIndex = null;

        for (int i = 0; i < keys.size(); i++) {
            BTreeKey<T> k = keys.get(i);
            if (key.compareTo(k) < 0) {
                keys.add(i, key);
                addedAtIndex = i;
                break;
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

        BTreeNode<T> result = null;
        if (keys.size() == degree) {
            // split
            if (isRoot()) {
                // creating new root
                parent = new BTreeNode<T>(degree);
                result = parent;
            }

            int medianIndex = getMedianKeyIndex();

            BTreeKey<T> medianKey = keys.get(medianIndex);

            splitKeysEitherSideOfMedianIntoTwoChildrenOfParent(medianIndex);

            keys.remove(medianIndex);

            BTreeNode<T> result2 = parent.add(medianKey);

            if (result2 != null)
                result = result2;

        } else
            result = null;

        return result;

    }

    /**
     * Returns true if and only if this is the root node of the BTree (has no
     * parent).
     * 
     * @return
     */
    private boolean isRoot() {
        return parent == null;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Node [keys=");
        builder.append(keys);
        builder.append("]");
        return builder.toString();
    }

}
