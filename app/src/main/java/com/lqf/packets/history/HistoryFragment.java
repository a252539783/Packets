package com.lqf.packets.history;

import com.lqf.packets.CommonFragment;
import com.lqf.packets.CommonPresenter;
import com.lqf.packets.R;

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
