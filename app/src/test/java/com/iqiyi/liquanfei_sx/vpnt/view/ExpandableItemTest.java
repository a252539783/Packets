package com.iqiyi.liquanfei_sx.vpnt.view;

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
        mRoot.mSize=400;
    }

    @Test
    public void nextInOneLayer()
    {
        for (int i=0;i<10000;i++)
        {
            int m= Math.abs(rand.nextInt())%400;
            assertEquals(m,mRoot.get(m)[0]);
        }
    }

}