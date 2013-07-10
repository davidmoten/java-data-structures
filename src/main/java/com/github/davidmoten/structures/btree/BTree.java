package com.github.davidmoten.structures.btree;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class BTree<T extends Serializable & Comparable<T>> implements
        Iterable<T>, Serializable {

    private static final long serialVersionUID = -1738319993570666751L;

    private Node<T> root;

    private final int degree;
    private final Optional<File> file;
    private final int keySize;
    private final static long POSITION_START = 1000;
    private long rootPosition = POSITION_START;

    /**
     * Constructor.
     * 
     * @param degree
     */
    private BTree(int degree, Optional<File> file, Class<T> cls, int keySize) {
        Preconditions.checkArgument(degree >= 2, "degree must be >=2");
        this.degree = degree;
        this.file = file;
        this.keySize = keySize;
        if (file.isPresent() && file.get().exists()) {
            readHeader();
            root = new NodeRef<T>(this, of(rootPosition),
                    Optional.<KeySide<T>> absent());
        } else {
            if (file.isPresent())
                writeHeader();
            root = new NodeRef<T>(this, Optional.<Long> absent(),
                    Optional.<KeySide<T>> absent());
        }
    }

    private void readHeader() {
        try {
            RandomAccessFile f = new RandomAccessFile(file.get(), "r");
            byte[] header = new byte[(int) POSITION_START];
            f.seek(0);
            f.read(header);
            f.close();
            ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(header));
            rootPosition = ois.readLong();
            ois.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void writeHeader() {
        try {
            if (!file.get().exists())
                file.get().createNewFile();
            RandomAccessFile f = new RandomAccessFile(file.get(), "rws");
            ByteArrayOutputStream header = new ByteArrayOutputStream(
                    (int) POSITION_START);
            ObjectOutputStream oos = new ObjectOutputStream(header);
            oos.writeLong(rootPosition);
            oos.close();
            f.seek(0);
            f.write(header.toByteArray());
            if (header.size() < POSITION_START) {
                byte[] more = new byte[(int) POSITION_START - header.size()];
                f.write(more);
            }
            f.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder<R extends Serializable & Comparable<R>> {
        private int degree = 100;
        private Optional<File> file = absent();
        private int keySize = 1000;
        private final Class<R> cls;

        public Builder(Class<R> cls) {
            this.cls = cls;
        }

        public Builder<R> degree(int degree) {
            this.degree = degree;
            return this;
        }

        public Builder<R> file(File file) {
            this.file = of(file);
            return this;
        }

        public Builder<R> keySize(int keySize) {
            this.keySize = keySize;
            return this;
        }

        public BTree<R> build() {
            return new BTree<R>(degree, file, cls, keySize);
        }
    }

    public static <R extends Comparable<R> & Serializable> Builder<R> builder(
            Class<R> cls) {
        return new Builder<R>(cls);
    }

    public int getDegree() {
        return degree;
    }

    /**
     * Adds an element to the b-tree. May replace root.
     * 
     * @param t
     */
    public void add(T t) {
        Optional<Node<T>> newRoot = root.add(t);
        if (newRoot.isPresent()) {
            root = newRoot.get();
            if (file.isPresent())
                writeHeader();
        }
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
        return null;
    }

    public long delete(T key) {
        return root.delete(key);
    }

    @VisibleForTesting
    List<? extends Key<T>> getKeys() {
        return root.getKeys();
    }

    @Override
    public Iterator<T> iterator() {
        return root.iterator();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BTree [root=");
        builder.append(root);
        builder.append("]");
        return builder.toString();
    }

    public Optional<File> getFile() {
        return file;
    }

    public int getKeySize() {
        return keySize;
    }

    public long nextPosition() {
        try {
            if (file.isPresent()) {

                if (!file.get().exists())
                    file.get().createNewFile();

                return file.get().length();

            } else
                return 0;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
