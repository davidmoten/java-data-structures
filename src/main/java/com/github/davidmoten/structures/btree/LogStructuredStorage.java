package com.github.davidmoten.structures.btree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.google.common.base.Preconditions;

public class LogStructuredStorage {

	private int currentFileNo = 1;
	private RandomAccessFile currentFile;
	private final long maxFileSize;
	private final File directory;

	public LogStructuredStorage(File directory, long maxFileSize) {
		this.directory = directory;
		this.maxFileSize = maxFileSize;
		nextFile();
	}

	private void nextFile() {
		try {
			File file = new File(directory, currentFileNo + ".lss");
			file.createNewFile();
			currentFile = new RandomAccessFile(file, "rws");
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized long append(byte[] bytes) {
		Preconditions.checkArgument(bytes.length < maxFileSize);
		try {
			long pos = currentFile.length();

			if (pos + bytes.length > maxFileSize) {
				currentFileNo++;
				nextFile();
			}
			currentFile.seek(pos);
			currentFile.write(bytes);
			return pos;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
