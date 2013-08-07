package com.github.davidmoten.structures.util;

import java.util.Deque;
import java.util.LinkedList;

import com.google.common.base.Optional;

public class ImmutableStack<T> {

	private final Deque<T> q;

	public ImmutableStack() {
		this(new LinkedList<T>());
	}

	private ImmutableStack(Deque<T> q) {
		this.q = new LinkedList<T>(q);
	}

	public ImmutableStack<T> push(T t) {
		LinkedList<T> q2 = new LinkedList<T>(q);
		q2.push(t);
		return new ImmutableStack<T>(q2);
	}

	public Optional<T> peek() {
		T value = q.peek();
		return Optional.fromNullable(value);
	}

	public ImmutableStack<T> pop() {
		LinkedList<T> q2 = new LinkedList<T>(q);
		q2.pop();
		return new ImmutableStack<T>(q2);
	}

	public boolean isEmpty() {
		return q.isEmpty();
	}

}
