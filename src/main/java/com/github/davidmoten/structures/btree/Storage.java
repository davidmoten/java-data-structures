package com.github.davidmoten.structures.btree;

import static com.google.common.base.Optional.of;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.List;

public class Storage {

	private final File file;

	public File getFile() {
		return file;
	}

	public Storage(File directory, String name) {
		this.file = new File(directory, name);
	}

	private long nextPosition() {
		try {
			if (!file.exists())
				file.createNewFile();
			return file.length();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized <T extends Serializable & Comparable<T>> void save(
			List<NodeRef<T>> saveQueue) {
		ByteArrayOutputStream allBytes = new ByteArrayOutputStream();
		long startPos = nextPosition();
		long pos = startPos;
		for (NodeRef<T> node : saveQueue) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			node.save(bytes);
			node.setPosition(of(pos));

			try {
				allBytes.write(bytes.toByteArray());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			pos += bytes.size();

			// TODO does node cache work without this line?
			// loaded(node.getPosition().get(), node);
		}
		saveToFile(allBytes.toByteArray(), startPos);
	}

	public <T extends Serializable & Comparable<T>> void load(NodeRef<T> node) {
		try {
			FileInputStream fis = new FileInputStream(file);
			fis.skip(node.getPosition().get());
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
	private void saveToFile(byte[] bytes, long pos) {
		try {
			RandomAccessFile f = new RandomAccessFile(file, "rw");
			f.seek(pos);
			f.write(bytes);
			f.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
