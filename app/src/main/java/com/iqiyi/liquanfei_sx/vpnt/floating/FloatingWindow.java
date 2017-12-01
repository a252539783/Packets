package com.iqiyi.liquanfei_sx.vpnt.floating;

import com.iqiyi.liquanfei_sx.vpnt.FakeFragment;

/**
 * Created by Administrator on 2017/9/28.
 */

public abstract class FloatingWindow extends FakeFragment{

    private WindowStack mStack;

    public FloatingWindow()
    {
        super();
    }

    void setWindowStack(WindowStack stack)
    {
        mStack=stack;
    }

    void startWindow(Class windowCls)
    {
        mStack.startWindow(windowCls);
    }

    void back()
    {
        mStack.backWindow();
    }

    public abstract void getWindowSize(int[] size);
}
