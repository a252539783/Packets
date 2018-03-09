package com.lqf.packets.saved;

import com.lqf.packets.CommonFragment;
import com.lqf.packets.CommonPresenter;
import com.lqf.packets.R;

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
