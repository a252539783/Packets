package com.iqiyi.liquanfei_sx.vpnt.history;

import com.iqiyi.liquanfei_sx.vpnt.CommonPresenter;
import com.iqiyi.liquanfei_sx.vpnt.PagerFragment;
import com.iqiyi.liquanfei_sx.vpnt.R;

/**
 * Created by Administrator on 2017/11/30.
 */

public class HistoryFragment2 extends PagerFragment {

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
