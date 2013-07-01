package com.github.davidmoten.structures;

public class BTreeKey<T extends Comparable<T>> implements
        Comparable<BTreeKey<T>> {

    private final T t;
    private BTreeNode<T> left;
    private BTreeNode<T> right;

    public BTreeKey(T t) {
        this.t = t;
    }

    public T value() {
        return t;
    }

    public BTreeNode<T> getLeft() {
        return left;
    }

    public void setLeft(BTreeNode<T> left) {
        this.left = left;
    }

    @Override
    public int compareTo(BTreeKey<T> o) {
        return t.compareTo(o.value());
    }

    public BTreeNode<T> getRight() {
        return right;
    }

    public void setRight(BTreeNode<T> right) {
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
