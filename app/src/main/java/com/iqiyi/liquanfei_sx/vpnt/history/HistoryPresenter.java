package com.iqiyi.liquanfei_sx.vpnt.history;

import android.view.View;

import com.iqiyi.liquanfei_sx.vpnt.R;
import com.iqiyi.liquanfei_sx.vpnt.CommonPresenter;
import com.iqiyi.liquanfei_sx.vpnt.packet.LocalPackets;
import com.iqiyi.liquanfei_sx.vpnt.packet.PersistRequest;
import com.iqiyi.liquanfei_sx.vpnt.view.ExpandableRecyclerView;

import java.util.List;

/**
 * Created by Administrator on 2017/11/6.
 */

public class HistoryPresenter extends CommonPresenter {

    private ExpandableRecyclerView mHistories;
    private HistoryAdapter mAdapter=new HistoryAdapter();

    @Override
    protected void onViewBind(View v) {
        mHistories=(ExpandableRecyclerView)v.findViewById(R.id.erv_history);

//        LocalPackets.mgr().addRequest(PersistRequest.newReadRequest(new PersistRequest.OnLoadHistoryListener() {
//            @Override
//            public void loadOne(int index) {
//            }
//
//            @Override
//            public void loadStart(List<LocalPackets.CaptureInfo> all) {
//                mAdapter.setHistorySource(all);
//                mHistories.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mHistories.setAdapter(mAdapter);
//                    }
//                });
//            }
//        }));
    }
}
