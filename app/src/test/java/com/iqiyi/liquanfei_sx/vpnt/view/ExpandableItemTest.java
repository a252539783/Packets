package com.iqiyi.liquanfei_sx.vpnt.view;

import com.iqiyi.liquanfei_sx.vpnt.Util;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/12/7.
 */
public class ExpandableItemTest {

    ExpandableItem mRoot;
    Random rand=new Random();

    @Before
    public void before()
    {
        mRoot=new ExpandableItem(-1,null);
    }

    @Test
    public void nextInOneLayer()
    {
        nextInOneLayer(400);
    }

    @Test
    public void fresh()
    {
        mRoot.fresh(15);
        nextInOneLayer(15);
        mRoot.get(14);
        mRoot.fresh(10);
        nextInOneLayer(10);
    }

    @Test
    public void expand()
    {
        mRoot.fresh(40);
        mRoot.expand(20,10);
        mRoot.expand(18,10);
        for (int i=0;i<60;i++)
        {
            Util.pln(mRoot.get(i));
        }
        for (int i=59;i>=0;i--)
        {
            Util.pln(mRoot.get(i));
        }
    }

    @Test
    public void insert()
    {
        mRoot.fresh(30);
        for (int i=0;i<30;i++)
    {
        Util.pln(mRoot.get(i));
    }
        mRoot.insert(30);
        for (int i=0;i<31;i++)
        {
            Util.pln(mRoot.get(i));
        }
    }

    void nextInOneLayer(int size)
    {
        for (int i=0;i<size*size;i++)
        {
            int m= Math.abs(rand.nextInt())%size;
            //System.out.println(""+i);
            assertEquals(m,mRoot.get(m)[0]);
        }
    }

}