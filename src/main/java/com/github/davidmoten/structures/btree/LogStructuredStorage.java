package com.github.davidmoten.structures.btree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class LogStructuredStorage {

	private final int currentFileNo = 1;
	private final RandomAccessFile currentFile;

	public LogStructuredStorage(File directory, long maxFileSize) {
		try {
			currentFile = new RandomAccessFile(new File(directory,
					currentFileNo + ".lss"), "rws");
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public long append(byte[] bytes) {
		try {
			long pos = currentFile.length();
			currentFile.seek(pos);
			currentFile.write(bytes);
			return pos;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
