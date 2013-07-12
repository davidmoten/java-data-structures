package com.github.davidmoten.structures.btree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;

import org.junit.Test;

public class ImmutableStackTest {

	@Test
	public void testNewStackIsEmpty() {
		ImmutableStack<Integer> s = new ImmutableStack<Integer>();
		assertTrue(s.isEmpty());
	}

	@Test(expected = NoSuchElementException.class)
	public void testEmptyStackThrowsExceptionOnPop() {
		ImmutableStack<Integer> s = new ImmutableStack<Integer>();
		s.pop();
	}

	@Test
	public void testEmptyStackThrowsExceptionOnPeekFirst() {
		ImmutableStack<Integer> s = new ImmutableStack<Integer>();
		assertFalse(s.peek().isPresent());
	}

	@Test
	public void testIsLastInFirstOut() {
		ImmutableStack<Integer> s = new ImmutableStack<Integer>();
		s = s.push(1);
		s = s.push(2);
		assertEquals(2, (int) s.peek().get());
		s = s.pop();
		assertEquals(1, (int) s.peek().get());
		s = s.pop();
		assertFalse(s.peek().isPresent());
	}

	@Test
	public void testIsNotEmptyWhenOneItemPushed() {
		ImmutableStack<Integer> s = new ImmutableStack<Integer>();
		s = s.push(1);
		assertFalse(s.isEmpty());
	}

}
