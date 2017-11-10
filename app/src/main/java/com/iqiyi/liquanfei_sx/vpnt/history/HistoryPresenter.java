package com.iqiyi.liquanfei_sx.vpnt.history;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.iqiyi.liquanfei_sx.vpnt.R;
import com.iqiyi.liquanfei_sx.vpnt.CommonPresenter;
import com.iqiyi.liquanfei_sx.vpnt.packet.LocalPackets;
import com.iqiyi.liquanfei_sx.vpnt.packet.PersistRequest;
import com.iqiyi.liquanfei_sx.vpnt.view.ExpandableRecyclerView;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Administrator on 2017/11/6.
 */

public class HistoryPresenter extends CommonPresenter implements LocalPackets.OnHistoryChangeListener{

    private ExpandableRecyclerView mHistories;
    private HistoryAdapter mAdapter;

    private H h=new H(this);

    @Override
    protected void onViewBind(View v) {
        mHistories=(ExpandableRecyclerView)v.findViewById(R.id.erv_history);
        mAdapter=new HistoryAdapter(v.getContext());
        //mHistories.setAdapter(mAdapter);

        LocalPackets.get().addHistoryChangeListener(this);
        LocalPackets.mgr().addRequest(PersistRequest.newReadRequest());
    }

    @Override
    public void onChange() {
        h.sendEmptyMessage(0);
    }

    private static class H extends Handler
    {
        WeakReference<HistoryPresenter> p;

        H(HistoryPresenter p)
        {
            this.p=new WeakReference<HistoryPresenter>(p);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            HistoryPresenter hp;
            if ((hp=p.get())==null)
                return ;

            hp.mAdapter.setHistorySource(LocalPackets.get().mAllPackets);
            hp.mHistories.setAdapter(hp.mAdapter);
        }
    }
}
