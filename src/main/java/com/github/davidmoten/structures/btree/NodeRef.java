package com.github.davidmoten.structures.btree;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Optional;

public class NodeRef<T extends Serializable & Comparable<T>> implements Node<T> {

    static final int CHILD_ABSENT = -1;

    private Optional<Long> position;

    private Optional<NodeActual<T>> node = Optional.absent();
    private final BTree<T> btree;
    private final Optional<KeySide<T>> parentKeySide;

    public NodeRef(BTree<T> btree, Optional<Long> position,
            Optional<KeySide<T>> parentKeySide) {
        this.parentKeySide = parentKeySide;
        this.btree = btree;
        this.position = position;
    }

    private synchronized Node<T> node() {
        if (!node.isPresent()) {
            if (position.isPresent()) {
                load();
            } else {
                position = of(btree.nextPosition());
                node = of(new NodeActual<T>(btree, parentKeySide,
                        position.get()));
            }
        }
        return node.get();
    }

    public void load() {

        node = of(new NodeActual<T>(btree, parentKeySide, position.get()));
        if (btree.getFile().isPresent()) {
            try {

                RandomAccessFile f = new RandomAccessFile(
                        btree.getFile().get(), "r");
                f.seek(position.get());
                int numBytes = btree.getDegree() * btree.getKeySize();
                byte[] b = new byte[numBytes];
                int numBytesRead = f.read(b);
                System.out.println("numBytesRead=" + numBytesRead);
                f.close();

                ByteArrayInputStream bytes = new ByteArrayInputStream(b);
                ObjectInputStream ois = new ObjectInputStream(bytes);
                int count = ois.readInt();
                Optional<Key<T>> previous = absent();
                Optional<Key<T>> first = absent();
                for (int i = 0; i < count; i++) {
                    @SuppressWarnings("unchecked")
                    T t = (T) ois.readObject();
                    long left = ois.readLong();
                    long right = ois.readLong();
                    boolean deleted = ois.readBoolean();
                    Key<T> key = new Key<T>(t);
                    if (left != CHILD_ABSENT)
                        key.setLeft(of((Node<T>) new NodeRef<T>(btree,
                                of(left), of(new KeySide<T>(key, Side.LEFT)))));
                    if (right != CHILD_ABSENT)
                        key.setRight(of((Node<T>) new NodeRef<T>(btree,
                                of(right), of(new KeySide<T>(key, Side.RIGHT)))));
                    key.setDeleted(deleted);
                    key.setNode(of((Node<T>) this));
                    key.setNext(Optional.<Key<T>> absent());
                    if (!first.isPresent())
                        first = of(key);
                    if (previous.isPresent())
                        previous.get().setNext(of(key));
                    previous = of(key);
                }
                node.get().setFirst(first);
                ois.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Optional<Node<T>> add(T t) {
        return node().add(t);
    }

    @Override
    public Optional<Node<T>> add(Key<T> key) {
        return node().add(key);
    }

    @Override
    public Optional<T> find(T t) {
        return node().find(t);
    }

    @Override
    public long delete(T t) {
        return node().delete(t);
    }

    @Override
    public List<? extends Key<T>> getKeys() {
        return node().getKeys();
    }

    @Override
    public void setFirst(Optional<Key<T>> first) {
        node().setFirst(first);
    }

    @Override
    public Optional<Key<T>> bottomLeft() {
        return node().bottomLeft();
    }

    @Override
    public Iterator<T> iterator() {
        return node().iterator();
    }

    @Override
    public String keysAsString() {
        return node().keysAsString();
    }

    @Override
    public Optional<KeySide<T>> getParentKeySide() {
        return node().getParentKeySide();
    }

    @Override
    public void setParentKeySide(Optional<KeySide<T>> parentKeySide) {
        node().setParentKeySide(parentKeySide);
    }

    @Override
    public String toString(String space) {
        return node().toString(space);
    }

    @Override
    public String toString() {
        if (node.isPresent()) {
            return node.toString();
        } else
            return asString();
    }

    public String asString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NodeRef [position=");
        builder.append(position);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public void save() {
        node().save();
    }

    @Override
    public long getPosition() {
        return node().getPosition();
    }

}
