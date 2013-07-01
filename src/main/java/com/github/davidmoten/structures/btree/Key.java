package com.github.davidmoten.structures.btree;

public class Key<T extends Comparable<T>> implements
        Comparable<Key<T>> {

    private final T t;
    private Node<T> left;
    private Node<T> right;

    public Key(T t) {
        this.t = t;
    }

    public T value() {
        return t;
    }

    public Node<T> getLeft() {
        return left;
    }

    public void setLeft(Node<T> left) {
        this.left = left;
    }

    @Override
    public int compareTo(Key<T> o) {
        return t.compareTo(o.value());
    }

    public Node<T> getRight() {
        return right;
    }

    public void setRight(Node<T> right) {
        this.right = right;
    }

    public boolean hasChild() {
        return left != null || right != null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Key [t=");
        builder.append(t);
        if (left != null) {
            builder.append(", left=");
            builder.append(left);
        }
        if (right != null) {
            builder.append(", right=");
            builder.append(right);
        }
        builder.append("]");
        return builder.toString();
    }

}
