package com.iqiyi.liquanfei_sx.vpnt.saved;

import com.iqiyi.liquanfei_sx.vpnt.CommonFragment;
import com.iqiyi.liquanfei_sx.vpnt.CommonPresenter;
import com.iqiyi.liquanfei_sx.vpnt.R;

/**
 * Created by Administrator on 2017/11/15.
 */

public class SavedFragment extends CommonFragment {
    @Override
    public int getLayout() {
        return R.layout.list_packets;
    }

    @Override
    public CommonPresenter getPresenter() {
        return new SavedPresenter();
    }
}
