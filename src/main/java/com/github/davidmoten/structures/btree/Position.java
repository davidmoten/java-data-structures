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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (fileNumber ^ (fileNumber >>> 32));
		result = prime * result + (int) (position ^ (position >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (fileNumber != other.fileNumber)
			return false;
		if (position != other.position)
			return false;
		return true;
	}
}
