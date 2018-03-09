package com.lqf.packets.saved;

import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lqf.packets.CommonPresenter;
import com.lqf.packets.IAdapter;
import com.lqf.packets.R;
import com.lqf.packets.packet.LocalPackets;
import com.lqf.packets.packet.PersistRequest;
import com.lqf.packets.view.ExpandableRecyclerView3;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2017/11/15.
 */

public class SavedPresenter extends CommonPresenter implements LocalPackets.OnSavedChangeListener,SwipeRefreshLayout.OnRefreshListener{

    private ExpandableRecyclerView3 mHistories;
    private SwipeRefreshLayout mFreshLayout;
    private IAdapter mAdapter;

    private boolean mIsBound=false;

    private H h=new H(this);

    @Override
    protected void onViewBind(View v) {
        mHistories=(ExpandableRecyclerView3)v.findViewById(R.id.erv_packets);
        mFreshLayout=(SwipeRefreshLayout)v.findViewById(R.id.fresh_packets);
        mFreshLayout.setOnRefreshListener(this);
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

    public void notifyFresh()
    {
        LocalPackets.get().clearSaved();
        ((SavedAdapter2)mAdapter).notifyFresh(0);
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
        if (mFreshLayout.isRefreshing())
        {
            mFreshLayout.setRefreshing(false);
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
        if (mFreshLayout.isRefreshing())
        {
            mFreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {
        notifyFresh();
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
