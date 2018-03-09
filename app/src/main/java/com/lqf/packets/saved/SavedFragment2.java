package com.lqf.packets.saved;

import com.lqf.packets.CommonPresenter;
import com.lqf.packets.FakeFragment;
import com.lqf.packets.R;

/**
 * Created by Administrator on 2017/11/30.
 */

public class SavedFragment2 extends FakeFragment {

    private SavedPresenter mP=null;

    @Override
    public int getLayout() {
        return R.layout.list_packets;
    }

    @Override
    public CommonPresenter getPresenter() {
        if (mP==null)
            mP=new SavedPresenter();

        return mP;
    }
}
