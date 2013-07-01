package com.github.davidmoten.structures.btree;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class BTree<T extends Comparable<T>> {

    private Node<T> root;

    public BTree(int degree) {
        Preconditions.checkArgument(degree >= 2, "degree must be >=2");
        root = new Node<T>(degree);
    }

    public void add(T key) {
        Node<T> newRoot = root.add(key);
        if (newRoot != null)
            root = newRoot;
    }

    public Optional<T> find(T key) {
        return root.find(key);
    }

    public long delete(T key) {
        return root.delete(key);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BTree [root=");
        builder.append(root);
        builder.append("]");
        return builder.toString();
    }

    @VisibleForTesting
    List<? extends Key<T>> getKeys() {
        return root.getKeys();
    }

    // private static enum Side {
    // LEFT, RIGHT;
    // }

    // private final static class NodeIndex<R extends Comparable<R>> {
    // public NodeIndex(Node<R> node, int index, Side side) {
    // super();
    // this.node = node;
    // this.index = index;
    // this.side = side;
    // }
    //
    // final Node<R> node;
    // final int index;
    // final Side side;
    //
    // public Key<R> getKey() {
    // return node.getKey(index);
    // }
    // }
    //
    // @Override
    // // TODO implement this
    // public Iterator<T> iterator() {
    // return new Iterator<T>() {
    //
    // private final Deque<NodeIndex<T>> stack = new LinkedList<NodeIndex<T>>();
    //
    // {
    // stack.add(new NodeIndex<T>(root, 0, Side.LEFT));
    // }
    //
    // @Override
    // public boolean hasNext() {
    // return !stack.isEmpty();
    // }
    //
    // @Override
    // public T next() {
    // // should return the current value pointed at by
    // // stack.peekLast();
    //
    // if (stack.isEmpty())
    // throw new NoSuchElementException();
    // NodeIndex<T> n = stack.peekLast();
    // if (!n.getKey().hasChild()) {
    // T value = n.getKey().value();
    // // leaf node
    // if (n.index < n.node.getKeys().size() - 1) {
    // stack.pop();
    // stack.push(new NodeIndex<T>(n.node, n.index + 1,
    // Side.LEFT));
    // } else if (n.index == n.node.getKeys().size() - 1
    // && n.side == Side.LEFT) {
    // stack.pop();
    // stack.push(new NodeIndex<T>(n.node, n.index, Side.RIGHT));
    // } else {
    // stack.pop();
    // }
    // return value;
    // } else {
    // if (n.side == Side.LEFT
    // && n.index < n.node.getKeys().size() - 1) {
    // stack.pop();
    // stack.push(new NodeIndex<T>(n.node, n.index + 1,
    // Side.LEFT));
    // stack.push(new NodeIndex<T>(n.getKey().getLeft(), 0,
    // Side.LEFT));
    // } else if (n.side == Side.LEFT
    // && n.index == n.node.getKeys().size() - 1) {
    // stack.pop();
    // stack.push(new NodeIndex<T>(n.node, n.index, Side.RIGHT));
    // } else {
    // // side == Side.RIGHT
    // stack.pop();
    // stack.push(new NodeIndex<T>(n.getKey().getRight(), 0,
    // Side.LEFT));
    // }
    // return next();
    // }
    //
    // }
    //
    // @Override
    // public void remove() {
    // // TODO
    // }
    // };
    // }
}
