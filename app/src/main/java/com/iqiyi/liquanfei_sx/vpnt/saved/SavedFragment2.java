package com.iqiyi.liquanfei_sx.vpnt.saved;

import com.iqiyi.liquanfei_sx.vpnt.CommonPresenter;
import com.iqiyi.liquanfei_sx.vpnt.FakeFragment;
import com.iqiyi.liquanfei_sx.vpnt.R;

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
