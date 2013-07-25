package com.github.davidmoten.structures.btree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Storage {

	private final File file;

	public Storage(File file) {
		this.file = file;
	}

	public long nextPosition() {
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

}
