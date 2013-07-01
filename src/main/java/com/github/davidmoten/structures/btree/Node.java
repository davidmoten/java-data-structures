package com.github.davidmoten.structures.btree;

import static com.google.common.base.Optional.absent;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class Node<T extends Comparable<T>> {

    private final int degree;
    private Optional<Node<T>> parent;
    private Optional<Key<T>> first;

    public Node(int degree, Optional<Node<T>> parent) {
        this.degree = degree;
        this.parent = parent;
        this.first = Optional.absent();
    }

    public Node(int degree) {
        this(degree, Optional.<Node<T>> absent());
    }

    /**
     * Adds the element t to the node. If root node of BTree is changed then
     * returns new root node otherwise returns null. Does not perform any
     * splitting.
     * 
     * @param t
     * @return
     */
    public Node<T> add(T t) {

        if (isLeafNode()) {
            return add(new Key<T>(t));
        } else
            return addToNonLeafNode(t);
    }

    private Node<T> addToNonLeafNode(T t) {
        Node<T> result = null;
        boolean added = false;
        Optional<Key<T>> key = first;
        Optional<Key<T>> last = first;
        while (key.isPresent()) {
            if (t.compareTo(key.get().value()) < 0) {
                // don't need to check that left is non-null because of
                // properties of b-tree
                result = key.get().getLeft().add(t);
                added = true;
                break;
            }
            last = key;
            key = key.get().next();
        }

        if (!added && first.isPresent()) {
            // don't need to check that left is non-null because of properties
            // of b-tree
            result = last.get().getRight().add(t);
        }
        return result;
    }

    /**
     * Returns true if and only this node is a leaf node (has no children).
     * Because of the properties of a b-tree only have to check if the first key
     * has a child.
     * 
     * @return
     */
    private boolean isLeafNode() {

        return !first.isPresent() || !first.get().hasChild();
    }

    /**
     * Inserts key into the list of keys in sorted order. The inserted key has
     * priority in terms of its children become the children of its neighbours
     * in the list of keys.
     * 
     * @param first
     * @param key
     */
    private Key<T> add(Optional<Key<T>> first, Key<T> key) {
        System.out.println("adding " + key + " to " + first);

        Optional<Key<T>> k = first;
        Optional<Key<T>> previous = absent();
        Optional<Key<T>> next = absent();
        while (k.isPresent()) {
            if (key.value().compareTo(k.get().value()) < 0) {
                if (previous.isPresent())
                    previous.get().setNext(Optional.of(key));
                key.setNext(k);
                next = k;
                break;
            }
            previous = k;
            k = k.get().next();
        }

        if (!next.isPresent() && previous.isPresent()) {
            previous.get().setNext(Optional.of(key));
        }

        Key<T> result;
        if (!previous.isPresent())
            result = key;
        else
            result = first.get();

        // update previous and following keys to the newly added one
        if (previous.isPresent()) {
            previous.get().setRight(key.getLeft());
        }
        if (next.isPresent()) {
            next.get().setLeft(key.getRight());
        }
        return result;
    }

    private int countKeys() {
        int count = 0;
        Optional<Key<T>> k = first;
        while (k.isPresent()) {
            count++;
            k = k.get().next();
        }
        return count;
    }

    /**
     * Adds the key to the node. If root node of BTree is changed then returns
     * new root node otherwise returns this.
     * 
     * @param key
     * @return
     */
    private Node<T> add(Key<T> key) {

        if (!first.isPresent()) {
            first = Optional.of(key);
            return this;
        }

        first = Optional.of(add(first, key));

        Node<T> result = null;
        int keyCount = countKeys();
        if (keyCount == degree) {
            // split
            if (isRoot()) {
                // creating new root
                parent = Optional.of(new Node<T>(degree));
                result = parent.get();
            }

            Key<T> medianKey = splitKeysEitherSideOfMedianIntoTwoChildrenOfParent(keyCount);

            Node<T> result2 = parent.get().add(medianKey);

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
        return !parent.isPresent();
    }

    private Key<T> splitKeysEitherSideOfMedianIntoTwoChildrenOfParent(
            int keyCount) {
        int medianNumber;
        if (keyCount % 2 == 1)
            medianNumber = keyCount / 2 + 1;
        else
            medianNumber = (keyCount - 1) / 2 + 1;

        // create child1
        Optional<Key<T>> key = first;
        int count = 1;
        Node<T> child1 = new Node<T>(degree, parent);
        child1.first = first;
        Optional<Key<T>> previous = absent();
        while (count < medianNumber) {
            previous = key;
            key = key.get().next();
            count++;
        }
        Key<T> medianKey = key.get();
        previous.get().setNext(Optional.<Key<T>> absent());

        Node<T> child2 = new Node<T>(degree, parent);
        child2.first = key.get().next();

        medianKey.setNext(Optional.<Key<T>> absent());
        medianKey.setLeft(child1);
        medianKey.setRight(child2);

        return medianKey;

    }

    @VisibleForTesting
    Optional<Key<T>> getFirst() {
        return first;
    }

    public Optional<T> find(T t) {
        boolean isLeaf = isLeafNode();
        Optional<Key<T>> key = first;
        Optional<Key<T>> last = first;
        while (key.isPresent()) {
            int compare = t.compareTo(key.get().value());
            if (compare < 0) {
                if (isLeaf)
                    return absent();
                else
                    return key.get().getLeft().find(t);
            } else if (compare == 0 && !key.get().isDeleted())
                return Optional.of(key.get().value());
            last = key;
            key = key.get().next();
        }
        if (!isLeaf && last.isPresent()) {
            Node<T> right = last.get().getRight();
            if (right != null)
                return right.find(t);
            else
                return absent();
        } else
            return absent();
    }

    /**
     * Marks all keys as deleted that equal t.
     * 
     * @param t
     * @return
     */
    public long delete(T t) {
        int count = 0;
        boolean isLeaf = isLeafNode();
        Optional<Key<T>> key = first;
        Optional<Key<T>> last = first;
        while (key.isPresent()) {
            int compare = t.compareTo(key.get().value());
            if (compare < 0) {
                if (isLeaf)
                    return 0;
                else
                    return key.get().getLeft().delete(t);
            } else if (compare == 0 && !key.get().isDeleted()) {
                count++;
                key.get().setDeleted(true);
            }
            last = key;
            key = key.get().next();
        }
        if (count > 0)
            return count;
        if (!isLeaf && last.isPresent()) {
            Node<T> right = last.get().getRight();
            if (right != null)
                return right.delete(t);
            else
                return 0;
        } else
            return 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Node [keys=");
        builder.append(first);
        builder.append("]");
        return builder.toString();
    }

    @VisibleForTesting
    List<? extends Key<T>> getKeys() {
        List<Key<T>> list = Lists.newArrayList();
        Optional<Key<T>> key = first;
        while (key.isPresent()) {
            list.add(key.get());
            key = key.get().next();
        }
        return list;
    }

}
