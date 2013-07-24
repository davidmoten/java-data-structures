package com.github.davidmoten.structures.btree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.common.base.Optional;

public class PositionManager {

	private final Optional<File> file;

	public PositionManager(Optional<File> file) {
		this.file = file;
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
