package com.iqiyi.liquanfei_sx.vpnt.saved;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.iqiyi.liquanfei_sx.vpnt.CommonPresenter;
import com.iqiyi.liquanfei_sx.vpnt.R;
import com.iqiyi.liquanfei_sx.vpnt.history.HistoryAdapter;
import com.iqiyi.liquanfei_sx.vpnt.history.HistoryPresenter;
import com.iqiyi.liquanfei_sx.vpnt.packet.LocalPackets;
import com.iqiyi.liquanfei_sx.vpnt.packet.PersistRequest;
import com.iqiyi.liquanfei_sx.vpnt.view.ExpandableRecyclerView;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2017/11/15.
 */

public class SavedPresenter extends CommonPresenter implements LocalPackets.OnSavedChangeListener {

    private ExpandableRecyclerView mHistories;
    private SavedAdapter mAdapter;

    private H h=new H(this);

    @Override
    protected void onViewBind(View v) {
        mHistories=(ExpandableRecyclerView)v.findViewById(R.id.erv_history);
        mAdapter=new SavedAdapter(v.getContext());
        //mHistories.setAdapter(mAdapter);

        LocalPackets.get().addSavedChangeListener(this);
        LocalPackets.mgr().addRequest(PersistRequest.newReadSavedRequest());
    }

    @Override
    public void onChange() {
        h.sendEmptyMessage(0);
    }

    private static class H extends Handler
    {
        WeakReference<SavedPresenter> p;

        H(SavedPresenter p)
        {
            this.p=new WeakReference<>(p);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            SavedPresenter hp;
            if ((hp=p.get())==null)
                return ;

            hp.mAdapter.setSavedSource(LocalPackets.get().mSavedPackets);
            hp.mHistories.setAdapter(hp.mAdapter);
        }
    }
}
