package com.github.davidmoten.structures.btree;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.LinkedList;

import com.google.common.base.Optional;

public class Storage {

	private final File file;

	public Storage(File file) {
		this.file = file;
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
			LinkedList<NodeRef<T>> saveQueue) {
		ByteArrayOutputStream allBytes = new ByteArrayOutputStream();
		long startPos = nextPosition();
		long pos = startPos;
		while (!saveQueue.isEmpty()) {
			NodeRef<T> node = saveQueue.removeLast();

			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			node.save(bytes);
			node.setPosition(Optional.of(pos));

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
