package com.github.davidmoten.structures.btree;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class Key<T extends Comparable<T>> {

    private final T t;
    private Optional<Node<T>> left = Optional.absent();
    private Optional<Node<T>> right = Optional.absent();
    private boolean deleted;
    private Optional<Key<T>> next = Optional.absent();

    public Optional<Key<T>> next() {
        return next;
    }

    public void setNext(Optional<Key<T>> next) {
        this.next = next;
    }

    public Key(T t) {
        this.t = t;
        this.deleted = false;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public T value() {
        return t;
    }

    public Optional<Node<T>> getLeft() {
        return left;
    }

    public void setLeft(Optional<Node<T>> left) {
        Preconditions.checkNotNull(left);
        this.left = left;
    }

    public Optional<Node<T>> getRight() {
        return right;
    }

    public void setRight(Optional<Node<T>> right) {
        Preconditions.checkNotNull(right);
        this.right = right;
    }

    public boolean hasChild() {
        return left.isPresent() || right.isPresent();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Key [t=");
        builder.append(t);
        if (left.isPresent()) {
            builder.append(", left=");
            builder.append(left);
        }
        if (right.isPresent()) {
            builder.append(", right=");
            builder.append(right);
        }
        if (next.isPresent()) {
            builder.append(", next=");
            builder.append(next.get());
        }
        builder.append("]");
        return builder.toString();
    }

}
