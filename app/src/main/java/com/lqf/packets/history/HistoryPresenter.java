package com.lqf.packets.history;

import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lqf.packets.IAdapter;
import com.lqf.packets.R;
import com.lqf.packets.CommonPresenter;
import com.lqf.packets.packet.LocalPackets;
import com.lqf.packets.packet.PersistRequest;
import com.lqf.packets.view.ExpandableRecyclerView3;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2017/11/6.
 */

public class HistoryPresenter extends CommonPresenter implements LocalPackets.OnHistoryChangeListener,SwipeRefreshLayout.OnRefreshListener{

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
        mAdapter=new HistoryAdapter2(v.getContext());

        LocalPackets.get().addHistoryChangeListener(this);
        mAdapter.setListeners();
        LocalPackets.mgr().addRequest(PersistRequest.newReadRequest());
    }

    @Override
    protected void onPause() {
//        LocalPackets.get().removeHistoryListener(this);
//
//        if (mAdapter!=null)
//            mAdapter.removeListeners();
    }

    @Override
    protected void onResume() {
        if (mAdapter!=null) {
            ((RecyclerView.Adapter)mAdapter).notifyDataSetChanged();
//            LocalPackets.get().addHistoryChangeListener(this);
//            if (mIsBound) {
//                mAdapter.setListeners();
//            }
        }
    }

    public void notifyFilterChanged()
    {
        mAdapter.onFilterChanged();
    }

    public void notifyFresh()
    {
        LocalPackets.get().clearHistory();
        ((HistoryAdapter2)mAdapter).notifyFresh(0);
        LocalPackets.mgr().addRequest(PersistRequest.newReadRequest());
    }

    @Override
    public void onChange() {
        if (h!=null)
        {
            h.sendEmptyMessage(0);
        }else
        {
            ((RecyclerView.Adapter)mAdapter).notifyDataSetChanged();
        }
        if (mFreshLayout.isRefreshing())
        {
            mFreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onAdd(int timeIndex) {
        if (h!=null)
        {
            h.sendEmptyMessage(0);
        }else
        {
            ((ExpandableRecyclerView3.Adapter)mAdapter).notifyItemAdd(timeIndex);
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
        WeakReference<HistoryPresenter> p;

        H(HistoryPresenter p)
        {
            this.p=new WeakReference<>(p);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            HistoryPresenter hp;
            if ((hp=p.get())==null)
                return ;

            hp.mAdapter.setSource(LocalPackets.get().mAllPackets);
            hp.mHistories.setAdapter((ExpandableRecyclerView3.Adapter)hp.mAdapter);
            hp.mIsBound=true;

            hp.mAdapter.setListeners();
            hp.h=null;
        }
    }
}
