package com.github.davidmoten.structures.btree;

import java.util.Iterator;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class BTree<T extends Comparable<T>> implements Iterable<T> {

    private Node<T> root;

    /**
     * Constructor.
     * 
     * @param degree
     */
    public BTree(int degree) {
        Preconditions.checkArgument(degree >= 2, "degree must be >=2");
        root = new Node<T>(degree);
    }

    /**
     * Adds an element to the b-tree. May replace root.
     * 
     * @param t
     */
    public void add(T t) {
        Optional<Node<T>> newRoot = root.add(t);
        if (newRoot.isPresent())
            root = newRoot.get();
    }

    /**
     * Returns the first T found that equals t from this b-tree.
     * 
     * @param t
     * @return
     */
    public Optional<T> find(T t) {
        return root.find(t);
    }

    /**
     * Returns the result of a range query.
     * 
     * @param t1
     * @param t2
     * @param op1
     * @param op2
     * @return
     */
    public Iterable<Optional<T>> find(T t1, T t2, ComparisonOperator op1,
            ComparisonOperator op2) {
        // TODO
        return null;
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

    @Override
    public Iterator<T> iterator() {
        return root.iterator();
    }
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
