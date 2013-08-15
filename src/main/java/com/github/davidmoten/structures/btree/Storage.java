package com.github.davidmoten.structures.btree;

import static com.google.common.base.Optional.of;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class Storage {

	private static final long maxFileSize = 5000000L;

	private File file;
	/**
	 * The number of the file being written to currently.
	 */
	private long fileNumber;

	private final File directory;

	private final String name;

	private final Cache<Long, MappedByteBuffer> fileCache;

	public Storage(File directory, String name, long fileNumber) {
		this.directory = directory;
		this.name = name;
		this.fileNumber = fileNumber;
		this.file = getFile(fileNumber);
		fileCache = CacheBuilder.newBuilder().maximumSize(5).build();

	}

	public Storage(File directory, String name) {
		this(directory, name, getLatestFileNumber(directory, name));
	}

	private static long getLatestFileNumber(File directory, final String name) {
		File[] files = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String nm) {
				return nm.startsWith(name) && nm.matches("\\.\\d+$");
			}
		});
		Long max = null;
		for (File file : files) {
			int i = file.getName().lastIndexOf(".");
			String numberPart = file.getName().substring(i + 1);
			long number = Long.parseLong(numberPart);
			if (max == null || number > max)
				max = number;
		}
		if (max == null)
			max = 0L;
		return max;
	}

	public long getFileNumber() {
		return fileNumber;
	}

	public File getFile() {
		return file;
	}

	private File getFile(long fileNumber) {
		return new File(directory, name + "." + fileNumber);
	}

	private Position nextPosition() {
		try {
			if (!file.exists())
				file.createNewFile();
			if (file.length() >= maxFileSize) {
				fileNumber++;
				file = getFile(fileNumber);
				if (file.exists())
					file.delete();
				file.createNewFile();
			}
			return new Position(fileNumber, file.length());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized <T extends Serializable & Comparable<T>> void save(
			List<NodeRef<T>> saveQueue) {
		ByteArrayOutputStream allBytes = new ByteArrayOutputStream();
		Position startPos = nextPosition();
		long pos = startPos.getPosition();
		for (NodeRef<T> node : saveQueue) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			node.save(bytes);
			node.setPosition(of(new Position(startPos.getFileNumber(), pos)));

			try {
				allBytes.write(bytes.toByteArray());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			pos += bytes.size();
		}
		saveToFile(allBytes.toByteArray(), startPos);
	}

	public <T extends Serializable & Comparable<T>> void load(NodeRef<T> node) {
		try {
			FileInputStream fis = new FileInputStream(getFile(node
					.getPosition().get().getFileNumber()));
			// TODO use position.fileNumber
			fis.skip(node.getPosition().get().getPosition());
			BufferedInputStream bis = new BufferedInputStream(fis, 1024);
			System.out.println("loading node from " + node.getPosition().get());
			node.load(bis);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Saves byte array to the startpos given in the file.
	 * 
	 * @param bytes
	 * @param pos
	 */
	private void saveToFile(byte[] bytes, Position pos) {
		try {
			RandomAccessFile f = new RandomAccessFile(
					getFile(pos.getFileNumber()), "rw");
			f.seek(pos.getPosition());
			f.write(bytes);
			f.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void markObsoleteNodes() {

		// for each file from oldest to newest
		// find an obsolete root node and mark all its children as obsolete
		try {
			RandomAccessFile f = new RandomAccessFile(file, "rw");
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void markForDeletion(Position position) {
		markForDeletion(getFile(position.getFileNumber()),
				position.getPosition());
	}

	static void markForDeletion(File file, long pos) {
		try {
			FileInputStream fis = new FileInputStream(file);
			fis.skip(pos);
			ObjectInputStream ois = new ObjectInputStream(fis);
			long lengthBytes = ois.readLong();
			boolean canDelete = ois.readBoolean();
			ois.close();

			if (!canDelete) {
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bytes);
				oos.writeLong(lengthBytes);
				oos.flush();
				// mark as deleteable
				oos.writeBoolean(true);
				oos.close();
				RandomAccessFile f = new RandomAccessFile(file, "w");
				f.seek(pos);
				f.write(bytes.toByteArray());
				f.close();
			}
			// skip 4 bytes, then 8 bytes of long
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	void markObsolete(Position position) {

	}

	public static void main(String[] args) throws IOException {
		RandomAccessFile f = new RandomAccessFile("target/temp.txt", "rw");
		FileChannel fc = f.getChannel();
		MappedByteBuffer m = fc.map(FileChannel.MapMode.READ_WRITE, 0, 1024);
		for (int i = 1; i <= 10000; i++) {
			if (i % m.limit() == 0)
				m.flip();
			m.put((byte) 1);

		}
		f.close();
	}

	public File getDirectory() {
		return directory;
	}

	public String getName() {
		return name;
	}
}
