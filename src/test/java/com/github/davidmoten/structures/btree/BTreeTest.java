package com.github.davidmoten.structures.btree;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class BTreeTest {

    private static final double PRECISION = 0.0001;

    /**
     * Given nothing
     * 
     * When I create a BTree of degree 1
     * 
     * Then an exception is thrown
     * 
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInstantiatingBTreeOfDegree1ThrowsException() {
        new BTree<Integer>(1);
    }

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
        Key<String> top = t.getKeys().get(0);
        assertEquals("2", top.value());
        assertEquals(1, top.getLeft().get().getKeys().size());
        assertEquals("1", top.getLeft().get().getKeys().get(0).value());
        assertEquals(1, top.getRight().get().getKeys().size());
        assertEquals("3", top.getRight().get().getKeys().get(0).value());
    }

    /**
     * Given an empty BTree<Integer> of degree 3
     * 
     * When I insert 2,1 in order
     * 
     * Then the top node is 1,2
     * 
     */
    @Test
    public void test2_5() {
        BTree<Integer> t = new BTree<Integer>(3);
        t.add(2);
        t.add(1);
        assertEquals(2, t.getKeys().size());
        assertEquals(1, (int) t.getKeys().get(0).value());
        assertEquals(2, (int) t.getKeys().get(1).value());
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
        Key<String> top = t.getKeys().get(0);
        assertEquals("4", top.getRight().get().getKeys().get(1).value());
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
        BTree<Integer> t = new BTree<Integer>(3);
        t.add(1);
        t.add(2);
        t.add(3);
        t.add(0);
        System.out.println(t);
        Key<Integer> top = t.getKeys().get(0);
        assertEquals(0, (int) top.getLeft().get().getKeys().get(0).value());
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
        Key<Double> top = t.getKeys().get(0);
        assertKeyValuesAre(Lists.newArrayList(0.0, 1.0), top.getLeft().get()
                .getKeys());
        assertKeyValuesAre(Lists.newArrayList(3.0), top.getRight().get()
                .getKeys());
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
                .getLeft().get().getKeys());
        assertKeyValuesAre(Lists.newArrayList(1.0), t.getKeys().get(0)
                .getRight().get().getKeys());
    }

    /**
     * <p>
     * Given an empty BTree<String> of degree 3
     * </p>
     * 
     * <p>
     * When I insert 1,2,3,4,5,6,7
     * </p>
     * 
     * <p>
     * Then returns this tree:
     * </p>
     * 
     * <pre>
     *     4
     *    /  \
     *   2     6
     *  / \   / \
     * 1  3  5   7
     * </pre>
     * 
     * <p>
     * This is worked example from wikipedia <a
     * href="http://en.wikipedia.org/wiki/B-tree">article</a>.
     * </p>
     * 
     */
    @Test
    public void test7() {
        BTree<Double> t = new BTree<Double>(3);
        t.add(1.0);
        t.add(2.0);
        System.out.println(t);
        t.add(3.0);
        System.out.println(t);
        t.add(4.0);
        System.out.println(t);
        t.add(5.0);
        System.out.println(t);
        t.add(6.0);
        System.out.println(t);
        t.add(7.0);
        System.out.println(t);
        assertKeyValuesAre(Lists.newArrayList(4.0), t.getKeys());
        assertKeyValuesAre(Lists.newArrayList(2.0), t.getKeys().get(0)
                .getLeft().get().getKeys());
        assertKeyValuesAre(Lists.newArrayList(6.0), t.getKeys().get(0)
                .getRight().get().getKeys());
        assertKeyValuesAre(Lists.newArrayList(1.0), t.getKeys().get(0)
                .getLeft().get().getKeys().get(0).getLeft().get().getKeys());
        assertKeyValuesAre(Lists.newArrayList(3.0), t.getKeys().get(0)
                .getLeft().get().getKeys().get(0).getRight().get().getKeys());
        assertKeyValuesAre(Lists.newArrayList(5.0), t.getKeys().get(0)
                .getRight().get().getKeys().get(0).getLeft().get().getKeys());
        assertKeyValuesAre(Lists.newArrayList(7.0), t.getKeys().get(0)
                .getRight().get().getKeys().get(0).getRight().get().getKeys());
        System.out.println("iterated=" + Iterables.toString(t));
    }

    /**
     * <p>
     * Given an empty BTree<String> of EVEN degree 4
     * </p>
     * 
     * <p>
     * When 1,2,3,4 are inserted in order
     * </p>
     * 
     * <p>
     * Then b-tree looks like:
     * </p>
     * 
     * <pre>
     *   2
     *  / \
     * 1   3,4
     * </pre>
     * 
     */
    @Test
    public void testSplitWhenDegreeIsEven() {
        BTree<Double> t = new BTree<Double>(4);
        t.add(1.0);
        t.add(2.0);
        t.add(3.0);
        t.add(4.0);
        assertKeyValuesAre(newArrayList(2.0), t.getKeys());
        assertKeyValuesAre(newArrayList(1.0), t.getKeys().get(0).getLeft()
                .get().getKeys());
        assertKeyValuesAre(newArrayList(3.0, 4.0), t.getKeys().get(0)
                .getRight().get().getKeys());
    }

    /**
     * <p>
     * Given a BTree<String> of degree 3 with 1,2,3,4,5,6,7 inserted
     * </p>
     * 
     * <p>
     * When I find 1 or 2 or 3 or 4 or 5 or 6 or 7
     * </p>
     * 
     * <p>
     * Then returns 1 or 2 or 3 or 4 or 5 or 6 or 7
     * </p>
     * 
     */
    @Test
    public void test8() {
        BTree<Double> t = new BTree<Double>(3);
        t.add(1.0);
        t.add(2.0);
        t.add(3.0);
        t.add(4.0);
        t.add(5.0);
        t.add(6.0);
        t.add(7.0);
        for (int i = 1; i <= 7; i++) {
            assertEquals(i, t.find((double) i).get(), PRECISION);
        }
    }

    /**
     * <p>
     * Given a BTree<String> of degree 3 with 1,2,3,4,5,6,7 inserted
     * </p>
     * 
     * <p>
     * When I find 0.5 or 1.5 or 7.5
     * </p>
     * 
     * <p>
     * Then returns absent
     * </p>
     * 
     */
    @Test
    public void test9() {
        BTree<Double> t = new BTree<Double>(3);
        t.add(1.0);
        t.add(2.0);
        t.add(3.0);
        t.add(4.0);
        t.add(5.0);
        t.add(6.0);
        t.add(7.0);
        assertEquals(Optional.absent(), t.find(0.5));
        assertEquals(Optional.absent(), t.find(1.5));
        assertEquals(Optional.absent(), t.find(7.5));
    }

    /**
     * <p>
     * Given an empty BTree<String> of degree 3
     * </p>
     * 
     * <p>
     * When I find any value
     * </p>
     * 
     * <p>
     * Then returns absent
     * </p>
     * 
     */
    @Test
    public void test10() {
        BTree<Double> t = new BTree<Double>(3);
        assertEquals(Optional.absent(), t.find(1.0));
    }

    /**
     * <p>
     * Given a BTree<String> of degree 4 with 1,2,3 inserted
     * </p>
     * 
     * <p>
     * When I delete 2
     * </p>
     * 
     * <p>
     * Then find 2 returns absent
     * </p>
     * 
     */
    @Test
    public void test11() {
        BTree<Double> t = new BTree<Double>(4);
        t.add(1.0);
        t.add(2.0);
        t.add(3.0);
        t.delete(2.0);
        assertEquals(Optional.absent(), t.find(2.0));
    }

    /**
     * <p>
     * Given a BTree<String> of degree 4 with 1,2,3 inserted
     * </p>
     * 
     * <p>
     * When I delete 1,2,3
     * </p>
     * 
     * <p>
     * Then find 2 returns absent
     * </p>
     * 
     */
    @Test
    public void test12() {
        BTree<Double> t = new BTree<Double>(4);
        t.add(1.0);
        t.add(2.0);
        t.add(3.0);
        t.delete(1.0);
        t.delete(2.0);
        t.delete(3.0);
        assertEquals(Optional.absent(), t.find(2.0));
    }

    /**
     * <p>
     * Given an empty BTree<Integer>
     * </p>
     * 
     * <p>
     * When iterate it
     * </p>
     * 
     * <p>
     * Then the iterator has no values
     * </p>
     * 
     */
    @Test
    public void testIteratorOnEmptyBTree() {
        BTree<Integer> t = new BTree<Integer>(4);
        assertFalse(t.iterator().hasNext());
    }

    /**
     * <p>
     * Given an BTree<Integer> with one value
     * </p>
     * 
     * <p>
     * When iterate it
     * </p>
     * 
     * <p>
     * Then the iterator returns that value only
     * </p>
     * 
     */
    @Test
    public void testIteratorOnBTreeWithOneValue() {
        BTree<Integer> t = new BTree<Integer>(4);
        t.add(1);
        Iterator<Integer> it = t.iterator();
        assertEquals(1, (int) it.next());
        assertFalse(it.hasNext());
    }

    /**
     * <p>
     * Given an BTree<Integer> with two values
     * </p>
     * 
     * <p>
     * When iterate it
     * </p>
     * 
     * <p>
     * Then the iterator returns those two values only
     * </p>
     * 
     */
    @Test
    public void testIteratorOnBTreeWithTwoValue() {
        BTree<Integer> t = new BTree<Integer>(4);
        t.add(1);
        t.add(2);
        Iterator<Integer> it = t.iterator();
        assertEquals(1, (int) it.next());
        assertEquals(2, (int) it.next());
        assertFalse(it.hasNext());
    }

    /**
     * <p>
     * Given an BTree<Integer> with 5 values
     * </p>
     * 
     * <p>
     * When iterate it
     * </p>
     * 
     * <p>
     * Then the iterator returns those values only
     * </p>
     * 
     */
    @Test
    public void testIteratorOnBTreeWith5Values() {
        BTree<Integer> t = new BTree<Integer>(3);
        t.add(1);
        t.add(2);
        t.add(3);
        t.add(4);
        t.add(5);
        Iterator<Integer> it = t.iterator();
        assertEquals(1, (int) it.next());
        assertEquals(2, (int) it.next());
        assertEquals(3, (int) it.next());
        assertEquals(4, (int) it.next());
        assertEquals(5, (int) it.next());
        assertFalse(it.hasNext());
    }

    /**
     * <p>
     * Given an BTree<Integer> with 7 values
     * </p>
     * 
     * <p>
     * When iterate it
     * </p>
     * 
     * <p>
     * Then the iterator returns that value only
     * </p>
     * 
     */
    @Test
    public void testIteratorOnBTreeWith7Values() {
        BTree<Integer> t = new BTree<Integer>(3);
        t.add(1);
        t.add(2);
        t.add(3);
        t.add(4);
        t.add(5);
        t.add(6);
        t.add(7);
        Iterator<Integer> it = t.iterator();
        assertEquals(1, (int) it.next());
        assertEquals(2, (int) it.next());
        assertEquals(3, (int) it.next());
        assertEquals(4, (int) it.next());
        assertEquals(5, (int) it.next());
        assertEquals(6, (int) it.next());
        assertEquals(7, (int) it.next());
        assertFalse(it.hasNext());
    }

    private static void assertKeyValuesAre(List<Double> expected,
            List<? extends Key<Double>> keys) {
        String msg = "expected " + expected + " but was " + keys;
        assertEquals(msg, expected.size(), keys.size());
        for (int i = 0; i < expected.size(); i++)
            assertEquals(msg, expected.get(i), keys.get(i).value(), PRECISION);
    }
}
