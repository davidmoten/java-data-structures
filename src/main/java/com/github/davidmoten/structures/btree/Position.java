package com.github.davidmoten.structures.btree;

class Position {

	private final long fileNumber;
	private final long position;

	Position(long fileNumber, long position) {
		super();
		this.fileNumber = fileNumber;
		this.position = position;
	}

	long getFileNumber() {
		return fileNumber;
	}

	long getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return "Position [fileNumber=" + fileNumber + ", position=" + position
				+ "]";
	}

}
