package com.github.davidmoten.structures.btree;

import com.google.common.base.Optional;

public class PersistenceOptions {

	private final String basename;
	private final int keySizeBytes;
	private final Optional<Long> cacheSize;

	private PersistenceOptions(Builder builder) {
		this.basename = builder.basename;
		this.keySizeBytes = builder.keySizeBytes;
		this.cacheSize = builder.cacheSize;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String basename = "index";
		private int keySizeBytes = 100;
		private Optional<Long> cacheSize = Optional.absent();

		public Builder() {

		}

		public Builder basename(String basename) {
			this.basename = basename;
			return this;
		}

		public Builder keySizeBytes(int keySizeBytes) {
			this.keySizeBytes = keySizeBytes;
			return this;
		}

		public Builder cacheSize(long cacheSize) {
			this.cacheSize = Optional.of(cacheSize);
			return this;
		}

		public PersistenceOptions build() {
			return new PersistenceOptions(this);
		}
	}

	public String getBasename() {
		return basename;
	}

	public int getKeySizeBytes() {
		return keySizeBytes;
	}

	public Optional<Long> getCacheSize() {
		return cacheSize;
	}

	@Override
	public String toString() {
		return "PersistenceOptions [basename=" + basename + ", keySizeBytes="
				+ keySizeBytes + ", cacheSize=" + cacheSize + "]";
	}

}
