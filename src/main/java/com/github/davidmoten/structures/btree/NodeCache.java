package com.github.davidmoten.structures.btree;

import java.io.Serializable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class NodeCache<T extends Serializable & Comparable<T>> {

	private final Cache<Long, Node<T>> nodeCache;

	public NodeCache(long maxNodesInMemory) {
		nodeCache = createNodeCache(maxNodesInMemory);
	}

	private Cache<Long, Node<T>> createNodeCache(long cacheSize) {
		return CacheBuilder.newBuilder().maximumSize(cacheSize)
				.removalListener(createRemovalListener()).build();
	}

	private RemovalListener<Long, Node<T>> createRemovalListener() {
		return new RemovalListener<Long, Node<T>>() {

			@Override
			public void onRemoval(
					RemovalNotification<Long, Node<T>> notification) {
				notification.getValue().unload();
			}
		};
	}

	public void put(long position, Node<T> node) {
		nodeCache.put(position, node);
	}

}
