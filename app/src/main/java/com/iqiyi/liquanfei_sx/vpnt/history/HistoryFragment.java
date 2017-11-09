package com.iqiyi.liquanfei_sx.vpnt.history;

import com.iqiyi.liquanfei_sx.vpnt.CommonFragment;
import com.iqiyi.liquanfei_sx.vpnt.CommonPresenter;
import com.iqiyi.liquanfei_sx.vpnt.R;

/**
 * Created by Administrator on 2017/11/8.
 */

public class HistoryFragment extends CommonFragment {

    private CommonPresenter mPresenter=new HistoryPresenter();

    @Override
    public int getLayout() {
        return R.layout.list_packets;
    }

    @Override
    public CommonPresenter getPresenter() {
        return mPresenter;
    }
}
