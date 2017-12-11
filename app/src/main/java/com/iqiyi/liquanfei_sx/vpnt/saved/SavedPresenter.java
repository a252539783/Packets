package com.iqiyi.liquanfei_sx.vpnt.saved;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.iqiyi.liquanfei_sx.vpnt.CommonPresenter;
import com.iqiyi.liquanfei_sx.vpnt.IAdapter;
import com.iqiyi.liquanfei_sx.vpnt.R;
import com.iqiyi.liquanfei_sx.vpnt.packet.LocalPackets;
import com.iqiyi.liquanfei_sx.vpnt.packet.PersistRequest;
import com.iqiyi.liquanfei_sx.vpnt.view.ExpandableRecyclerView2;
import com.iqiyi.liquanfei_sx.vpnt.view.ExpandableRecyclerView3;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2017/11/15.
 */

public class SavedPresenter extends CommonPresenter implements LocalPackets.OnSavedChangeListener {

    private ExpandableRecyclerView3 mHistories;
    private IAdapter mAdapter;

    private boolean mIsBound=false;

    private H h=new H(this);

    @Override
    protected void onViewBind(View v) {
        mHistories=(ExpandableRecyclerView3)v.findViewById(R.id.erv_history);
        mAdapter=new SavedAdapter2(v.getContext());

        LocalPackets.get().addSavedChangeListener(this);
        mAdapter.setListeners();

        LocalPackets.mgr().addRequest(PersistRequest.newReadSavedRequest());
    }

    public void notifyFilterChanged()
    {
        mAdapter.onFilterChanged();
        LocalPackets.mgr().addRequest(PersistRequest.newReadSavedRequest());
    }

    @Override
    protected void onPause() {
//        LocalPackets.get().removeSavedListener(this);
//
//        if (mAdapter!=null)
//            mAdapter.removeListeners();
    }

    @Override
    protected void onResume() {
        if (mAdapter!=null) {
            ((RecyclerView.Adapter)mAdapter).notifyDataSetChanged();
//            LocalPackets.get().addSavedChangeListener(this);
//            LocalPackets.mgr().addRequest(PersistRequest.newReadSavedRequest());
//            if (mIsBound) {
//                mAdapter.setListeners();
//            }
        }
    }

    @Override
    public void onChange() {
        if (h!=null)
        {
            h.sendEmptyMessage(0);
        }else
        {
            ((SavedAdapter2)mAdapter).notifyFresh(0);
        }
    }

    @Override
    public void onAdd(int index) {
        if (h!=null)
        {
            h.sendEmptyMessage(0);
        }else
        {
            ((ExpandableRecyclerView3.Adapter)mAdapter).notifyItemAdd(index);
        }
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

            hp.mAdapter.setSource(LocalPackets.get().mSavedPackets);
            hp.mHistories.setAdapter((SavedAdapter2)hp.mAdapter);
            hp.mIsBound=true;
            hp.h=null;
        }
    }
}
