package com.github.davidmoten.structures.btree;

import java.io.Serializable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class NodeCache<T extends Serializable & Comparable<T>> {

	private final Cache<Long, NodeRef<T>> nodeCache;

	public NodeCache(long maxNodesInMemory) {
		nodeCache = createNodeCache(maxNodesInMemory);
		System.out.println("built NodeCache of size " + maxNodesInMemory);
	}

	private Cache<Long, NodeRef<T>> createNodeCache(long maxNodesInMemory) {
		return CacheBuilder.newBuilder().maximumSize(maxNodesInMemory)
				.removalListener(createRemovalListener()).build();
	}

	private RemovalListener<Long, NodeRef<T>> createRemovalListener() {
		return new RemovalListener<Long, NodeRef<T>>() {

			@Override
			public void onRemoval(
					RemovalNotification<Long, NodeRef<T>> notification) {
				notification.getValue().unload();
			}
		};
	}

	// TODO use filenumber in position
	public void put(long position, NodeRef<T> node) {
		nodeCache.put(position, node);
	}

}
