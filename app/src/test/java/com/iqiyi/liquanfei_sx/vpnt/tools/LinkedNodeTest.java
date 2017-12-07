package com.iqiyi.liquanfei_sx.vpnt.tools;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/12/6.
 */
public class LinkedNodeTest {

    LinkedNode<Long> root;

    @Before
    public void init()
    {
        root=new LinkedNode<>(1l);
    }

    @Test
    public void linkThisAfter() throws Exception {
        root.linkThisAfter(new LinkedNode<Long>(2l));
        assertEquals((long)2,(long)root.previous.o);
    }

    @Test
    public void replaceThisNext() throws Exception {
        root.linkThisBefore(new LinkedNode<Long>(2l)).linkThisBefore(3l);//1 2 3
        root.replaceThisNext(new LinkedNode<Long>(3l));// 1 3 3
        assertEquals((long)3,(long)root.next.o);
        assertEquals(3l,(long)root.next.next.o);
    }

    @Test
    public void replaceThisPrevious() throws Exception {
        root.linkThisAfter(5l).linkThisAfter(4l);//4 5 1
        root.replaceThisPrevious(3l);
        assertEquals(3l,(long)root.previous.o);
        assertEquals(4,(long)root.previous.previous.o);
    }

    @Test
    public void linkThisBefore() throws Exception {
        root.linkThisBefore(2l).linkThisBefore(3l);// 1 2 3

        assertEquals(2l,(long)root.next.o);
        assertEquals(3,(long)root.next.next.o);
    }

    @Test
    public void linkBetween() throws Exception {
        root.linkBetween(2l,3l);
        assertEquals(2l,(long)root.previous.o);
        assertEquals(3,(long)root.next.o);
    }

}