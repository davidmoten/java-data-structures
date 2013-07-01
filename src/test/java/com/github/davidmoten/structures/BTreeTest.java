package com.github.davidmoten.structures;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class BTreeTest {

    private static final double PRECISION = 0.0001;

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
        t.add("2");
        t.add("3");
        t.add("4");
        BTreeKey<String> top = t.getKeys().get(0);
        assertEquals("4", top.getRight().getKeys().get(1).value());
    }

    /**
     * Given an empty BTree<String> of degree 3 with inserted values 1,2,3 in
     * order.
     * 
     * When I insert 0
     * 
     * Then the node containing 1 has 0 inserted at start.
     * 
     */
    @Test
    public void test4() {
        BTree<String> t = new BTree<String>(3);
        t.add("1");
        t.add("2");
        t.add("3");
        t.add("0");
        System.out.println(t);
        BTreeKey<String> top = t.getKeys().get(0);
        assertEquals("0", top.getLeft().getKeys().get(0).value());
    }

    /**
     * Given an empty BTree<String> of degree 3 with inserted values 1,2,3 in
     * order.
     * 
     * When I insert 0
     * 
     * Then the node containing 1 has 0 inserted at start.
     * 
     */
    @Test
    public void test5() {
        BTree<Double> t = new BTree<Double>(3);
        t.add(1.0);
        t.add(2.0);
        t.add(3.0);
        t.add(0.0);
        System.out.println(t);
        BTreeKey<Double> top = t.getKeys().get(0);
        assertKeyValuesAre(Lists.newArrayList(0.0, 1.0), top.getLeft()
                .getKeys());
        assertKeyValuesAre(Lists.newArrayList(3.0), top.getRight().getKeys());
    }

    /**
     * Given an empty BTree<String> of degree 3 with inserted values 1,2,3,0 in
     * order.
     * 
     * When I insert 0.5
     * 
     * Then the root=0.5,2.0, node.left=0, node.right=1
     * 
     */
    @Test
    public void test6() {
        BTree<Double> t = new BTree<Double>(3);
        t.add(1.0);
        t.add(2.0);
        t.add(3.0);
        t.add(0.0);
        t.add(0.5);
        System.out.println(t);
        assertKeyValuesAre(Lists.newArrayList(0.5, 2.0), t.getKeys());
        assertKeyValuesAre(Lists.newArrayList(0.0), t.getKeys().get(0)
                .getLeft().getKeys());
        assertKeyValuesAre(Lists.newArrayList(1.0), t.getKeys().get(0)
                .getRight().getKeys());
    }

    private static void assertKeyValuesAre(List<Double> expected,
            List<? extends BTreeKey<Double>> keys) {
        String msg = "expected " + expected + " but was " + keys;
        assertEquals(msg, expected.size(), keys.size());
        for (int i = 0; i < expected.size(); i++)
            assertEquals(msg, expected.get(i), keys.get(i).value(), PRECISION);
    }
}
