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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * A standard BTree implementation as per wikipedia entry with some tweaks to
 * add in concurrency. In particular, iteration through the btree can be done
 * concurrently with addition/deletion with the side-effect that the iteration
 * might include changes to the tree since the iterator was created.
 * 
 * @author dxm
 * 
 * @param <T>
 */
public class BTree<T extends Serializable & Comparable<T>> implements
		Iterable<T> {

	/**
	 * The root node.
	 */
	private Node<T> root;

	/**
	 * The maximum number of keys in a node plus one.
	 */
	private final int degree;

	/**
	 * The file the btree is persisted to.
	 */
	private final Optional<File> file;

	/**
	 * The maximum number of bytes required to serialize T.
	 */
	private final int keySize;

	/**
	 * Where node storage starts in the file.
	 */
	private final static long NODE_STORAGE_START = 1000;

	/**
	 * The current position in the file of the root node.
	 */
	private long rootPosition = NODE_STORAGE_START;
	/**
	 * Manages allocation of file positions for nodes.
	 */
	private final PositionManager positionManager;

	/**
	 * This object is synchronized on to ensure that adds and deletes happen one
	 * at a time (synchronously).
	 */
	private final Object writeMonitor = new Object();

	private final Cache<Long, Node<T>> nodeCache = CacheBuilder.newBuilder()
			.maximumSize(20L).removalListener(createRemovalListener()).build();

	/**
	 * Constructor.
	 * 
	 * @param degree
	 */
	private BTree(int degree, Optional<File> file, Class<T> cls, int keySize) {
		Preconditions.checkArgument(degree >= 2, "degree must be >=2");
		Preconditions.checkArgument(keySize > 0, "keySize must be >0");
		Preconditions.checkNotNull(file, "file cannot be null");
		this.degree = degree;
		this.file = file;
		this.positionManager = new PositionManager(file);
		this.keySize = keySize;
		if (file.isPresent() && file.get().exists()) {
			readHeader();
			root = new NodeRef<T>(this, of(rootPosition));
		} else {
			if (file.isPresent())
				writeHeader();
			root = new NodeRef<T>(this, Optional.<Long> absent());
		}
	}

	private RemovalListener<Long, Node<T>> createRemovalListener() {
		return new RemovalListener<Long, Node<T>>() {

			@Override
			public void onRemoval(
					RemovalNotification<Long, Node<T>> notification) {
				notification.getValue().unload();
			}
		};
	}

	void loaded(long position, Node<T> node) {
		nodeCache.put(position, node);
	}

	/**
	 * Reads the header information from the file including the position of the
	 * root node.
	 */
	private void readHeader() {
		try {
			RandomAccessFile f = new RandomAccessFile(file.get(), "r");
			byte[] header = new byte[(int) NODE_STORAGE_START];
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

	/**
	 * Writes the header information to the file including the position of the
	 * root node.
	 */
	private synchronized void writeHeader() {
		try {
			if (!file.get().exists())
				file.get().createNewFile();
			RandomAccessFile f = new RandomAccessFile(file.get(), "rws");
			ByteArrayOutputStream header = new ByteArrayOutputStream(
					(int) NODE_STORAGE_START);
			ObjectOutputStream oos = new ObjectOutputStream(header);
			oos.writeLong(rootPosition);
			oos.close();
			f.seek(0);
			f.write(header.toByteArray());
			if (header.size() < NODE_STORAGE_START) {
				byte[] more = new byte[(int) NODE_STORAGE_START - header.size()];
				f.write(more);
			}
			f.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Builder for a {@link BTree}.
	 * 
	 * @author dxm
	 * 
	 * @param <R>
	 */
	public static class Builder<R extends Serializable & Comparable<R>> {
		private int degree = 100;
		private Optional<File> file = absent();
		private int keySize = 1000;
		private final Class<R> cls;

		/**
		 * Constructor.
		 * 
		 * @param cls
		 */
		public Builder(Class<R> cls) {
			this.cls = cls;
		}

		/**
		 * Sets the degree.
		 * 
		 * @param degree
		 * @return
		 */
		public Builder<R> degree(int degree) {
			this.degree = degree;
			return this;
		}

		/**
		 * Sets the file.
		 * 
		 * @param file
		 * @return
		 */
		public Builder<R> file(File file) {
			this.file = of(file);
			return this;
		}

		/**
		 * Sets the keySize.
		 * 
		 * @param keySize
		 * @return
		 */
		public Builder<R> keySize(int keySize) {
			this.keySize = keySize;
			return this;
		}

		/**
		 * Returns a new {@link BTree}.
		 * 
		 * @return
		 */
		public BTree<R> build() {
			return new BTree<R>(degree, file, cls, keySize);
		}
	}

	/**
	 * Creates a {@link Builder}.
	 * 
	 * @param cls
	 * @return
	 */
	public static <R extends Comparable<R> & Serializable> Builder<R> builder(
			Class<R> cls) {
		return new Builder<R>(cls);
	}

	/**
	 * Returns the degree (the max number of keys in a node plus one).
	 * 
	 * @return
	 */
	public int getDegree() {
		return degree;
	}

	/**
	 * Adds one or more elements to the b-tree. May replace root.
	 * 
	 * @param t
	 */
	public BTree<T> add(T... values) {
		for (T t : values)
			addOne(t);
		return this;
	}

	/**
	 * Adds a single element to the btree.
	 * 
	 * @param t
	 */
	private void addOne(T t) {
		synchronized (writeMonitor) {
			Optional<Node<T>> newRoot = root.add(t,
					new ImmutableStack<Node<T>>());
			if (newRoot.isPresent()) {
				root = newRoot.get();
				rootPosition = newRoot.get().getPosition();
				if (file.isPresent())
					writeHeader();
			}
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

	/**
	 * Deletes (or marks as deleted) those keys in the BTree that match one of
	 * the keys in the parameter.
	 * 
	 * @param keys
	 * @return
	 */
	public long delete(T... keys) {
		long count = 0;
		for (T key : keys)
			count += deleteOne(key);
		return count;
	}

	/**
	 * Deletes (or marks as deleted) all keys in the BTree that equal
	 * <code>key</code>.
	 * 
	 * @param key
	 * @return
	 */
	private long deleteOne(T key) {
		synchronized (writeMonitor) {
			return root.delete(key);
		}
	}

	/**
	 * Returns the file the btree is being persisted to. Returns
	 * Optional.absent() if none defined.
	 * 
	 * @return
	 */
	public Optional<File> getFile() {
		return file;
	}

	/**
	 * Returns the key size in bytes.
	 * 
	 * @return
	 */
	public int getKeySize() {
		return keySize;
	}

	/**
	 * Returns the {@link PositionManager} for this btree.
	 * 
	 * @return
	 */
	PositionManager getPositionManager() {
		return positionManager;
	}

	/**
	 * Returns the keys as a {@link List}.
	 * 
	 * @return
	 */
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

}
