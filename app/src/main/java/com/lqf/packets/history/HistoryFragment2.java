package com.lqf.packets.history;

import com.lqf.packets.CommonPresenter;
import com.lqf.packets.FakeFragment;
import com.lqf.packets.R;

/**
 * Created by Administrator on 2017/11/30.
 */

public class HistoryFragment2 extends FakeFragment {

    private HistoryPresenter mP=null;

    @Override
    public int getLayout() {
        return R.layout.list_packets;
    }

    @Override
    public CommonPresenter getPresenter() {
        if (mP==null)
            mP=new HistoryPresenter();

        return mP;
    }
}
