package com.iqiyi.liquanfei_sx.vpnt.saved;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.iqiyi.liquanfei_sx.vpnt.packet.LocalPackets;
import com.iqiyi.liquanfei_sx.vpnt.view.ExpandableRecyclerView;

import java.util.List;

/**
 * Created by Administrator on 2017/11/15.
 */

public class SavedAdapter extends ExpandableRecyclerView.Adapter {

    private List<LocalPackets.CaptureInfo> mAllHistory=null;
    private LayoutInflater mLf;
    private Context mContext;

    public SavedAdapter(Context c)
    {
        mContext=c;
        mLf=LayoutInflater.from(c);
    }

    @Override
    public void onBindExpandView(ExpandableRecyclerView view, int position) {

    }

    @Override
    public boolean canExpand(int position) {
        return false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }


}
