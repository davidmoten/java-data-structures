package com.github.davidmoten.structures.btree;

import java.io.Serializable;
import java.util.Iterator;

import com.google.common.base.Optional;

public class Util {
	public static <T extends Serializable & Comparable<T>> Iterable<Key<T>> keys(
			final Optional<Key<T>> first) {
		return new Iterable<Key<T>>() {

			@Override
			public Iterator<Key<T>> iterator() {
				return new Iterator<Key<T>>() {
					Optional<Key<T>> key = first;

					@Override
					public boolean hasNext() {
						return key.isPresent();
					}

					@Override
					public Key<T> next() {
						Key<T> result = key.get();
						key = key.get().next();
						return result;
					}

					@Override
					public void remove() {
						throw new RuntimeException("remove not implemented");
					}
				};
			}
		};
	}
}
