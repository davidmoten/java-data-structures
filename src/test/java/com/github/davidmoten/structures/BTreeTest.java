package com.github.davidmoten.structures;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BTreeTest {

    /**
     * Given an empty BTree<String> of degree 3
     * 
     * When I insert string "1"
     * 
     * Then the node is inserted as the first key in the first node of the BTree
     * 
     * When I insert string "2"
     * 
     * Then the node is inserted as the second key in the first node of the
     * BTree after "2"
     * 
     */
    @Test
    public void test1() {
        BTree<String> t = new BTree<String>(3);
        t.add("1");
        assertEquals(1, t.getKeys().size());
        assertEquals("1", t.getKeys().get(0).value());
        t.add("2");
        assertEquals(2, t.getKeys().size());
        assertEquals("1", t.getKeys().get(0).value());
        assertEquals("2", t.getKeys().get(1).value());
    }

    /**
     * Given an empty BTree<String> of degree 3
     * 
     * When I insert strings "1","2,"3" in order
     * 
     * Then the top node is {"2"}, left is node {"1"}, right is node {"3"}
     * 
     */
    @Test
    public void test2() {
        BTree<String> t = new BTree<String>(3);
        t.add("1");
        t.add("2");
        t.add("3");
        assertEquals(1, t.getKeys().size());
        BTreeKey<String> top = t.getKeys().get(0);
        assertEquals("2", top.value());
        assertEquals(1, top.getLeft().getKeys().size());
        assertEquals("1", top.getLeft().getKeys().get(0).value());
        assertEquals(1, top.getRight().getKeys().size());
        assertEquals("3", top.getRight().getKeys().get(0).value());
    }

    /**
     * Given an empty BTree<String> of degree 3 with inserted values 1,2,3 in
     * order.
     * 
     * When I insert 4
     * 
     * Then the node containing 3 has 4 added to it.
     * 
     */
    @Test
    public void test3() {
        BTree<String> t = new BTree<String>(3);
        t.add("1");
        System.out.println(t);
        t.add("2");
        System.out.println(t);
        t.add("3");
        System.out.println(t);
        t.add("4");
        System.out.println(t);
        BTreeKey<String> top = t.getKeys().get(0);
        assertEquals("4", top.getRight().getKeys().get(1).value());
    }
}
