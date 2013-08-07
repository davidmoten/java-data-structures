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

	private static final long maxFileSize = 5000000L;

	private File file;
	/**
	 * The number of the file being written to currently.
	 */
	private long fileNumber;

	private final File directory;

	private final String name;

	public Storage(File directory, String name, long fileNumber) {
		this.directory = directory;
		this.name = name;
		this.fileNumber = fileNumber;
		this.file = getFile(fileNumber);
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
}
