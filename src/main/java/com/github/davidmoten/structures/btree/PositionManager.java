package com.github.davidmoten.structures.btree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

import com.google.common.base.Optional;

public class PositionManager {

	private final Optional<File> file;
	private final Deque<Long> releasedPositions = new LinkedList<Long>();

	public PositionManager(Optional<File> file) {
		this.file = file;
	}

	public void releaseNodePosition(long position) {
		synchronized (releasedPositions) {
			releasedPositions.push(position);
		}
	}

	public long nextPosition() {
		try {
			if (file.isPresent()) {
				if (!file.get().exists())
					file.get().createNewFile();
				synchronized (releasedPositions) {
					if (!releasedPositions.isEmpty())
						return releasedPositions.pop();
					else
						return file.get().length();
				}
			} else
				return 0;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
