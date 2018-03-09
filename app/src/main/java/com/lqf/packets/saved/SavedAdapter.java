package com.lqf.packets.saved;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lqf.packets.R;
import com.lqf.packets.packet.LocalPackets;
import com.lqf.packets.packet.PersistRequest;
import com.lqf.packets.packet.TCPPacket;
import com.lqf.packets.tools.AppPortList;
import com.lqf.packets.view.ExpandableRecyclerView;

import java.util.List;

/**
 * Created by Administrator on 2017/11/15.
 */

public class SavedAdapter extends ExpandableRecyclerView.Adapter<SavedAdapter.H1> implements LocalPackets.OnSavedChangeListener{

    private List<LocalPackets.SavedInfo> mAllSaved =null;
    private LayoutInflater mLf;
    private Context mContext;

    public SavedAdapter(Context c)
    {
        mContext=c;
        mLf=LayoutInflater.from(c);
    }

    void setSavedSource(List<LocalPackets.SavedInfo> src)
    {
        mAllSaved =src;
    }

    @Override
    public void onBindExpandView(ExpandableRecyclerView view, int position) {
        final SavedItemAdapter adapter=new SavedItemAdapter(position);
        //mAdapters.put(position,adapter);
        view.setAdapter(adapter);
        LocalPackets.get().addSavedItemChangeListener(adapter);
        LocalPackets.mgr().addRequest(PersistRequest.newReadSavedRequest(mAllSaved.get(position).mUid));
    }

    @Override
    public boolean canExpand(int position) {
        return true;
    }

    @Override
    public SavedAdapter.H1 onCreateViewHolder(ViewGroup parent, int viewType) {
        return new H1(mLf.inflate(R.layout.item_packets,parent,false));
    }

    @Override
    public void onBindViewHolder(SavedAdapter.H1 holder, int position) {
        LocalPackets.SavedInfo si= mAllSaved.get(position);
        AppPortList.AppInfo info=si.mInfo;
        if (info!=null) {
            holder.mIcon.setImageDrawable(info.icon);
            holder.mName.setText(info.appName + ":" + info.info.applicationInfo.packageName);
        }
        holder.mNum.setText(si.mNum+"个");
    }

    @Override
    public int getItemCount() {
        return mAllSaved.size();
    }

    @Override
    public void onChange() {
        notifyDataSetChanged();
    }

    @Override
    public void onAdd(int index) {

    }

    private class SavedItemAdapter extends ExpandableRecyclerView.Adapter<H2> implements LocalPackets.OnSavedItemChangeListener
    {
        private int mIndex;
        private List<LocalPackets.SavedItem> mPackets;

        SavedItemAdapter(int index)
        {
            mIndex=index;
            mPackets=mAllSaved.get(index).mPackets;
        }

        @Override
        public void onBindExpandView(ExpandableRecyclerView view, int position) {
        }

        @Override
        public boolean canExpand(int position) {
            return false;
        }

        @Override
        public H2 onCreateViewHolder(ViewGroup parent, int viewType) {
            return new H2(mLf.inflate(R.layout.item_packet,parent,false));
        }

        @Override
        public void onBindViewHolder(H2 holder, int position) {
            TCPPacket packet=mPackets.get(position).mPackets.get(0).mPacket;
            holder.text.setText(packet.getIpInfo().getHeader()+new String(packet.getRawData(),packet.mOffset,packet.getDataLength()));
            holder.itemView.setId(position);
        }

        @Override
        public int getItemCount() {
            return mPackets.size();
        }

        @Override
        public void onChange(int time) {
            if (time==mIndex)
                notifyDataSetChanged();
        }

        @Override
        public void onAdd(int time, int index) {
            if (time==mIndex)
                notifyItemInserted(index);
        }
    }

    static class H1 extends RecyclerView.ViewHolder
    {
        private ImageView mIcon,mArrow;
        private TextView mName;
        private TextView mNum;

        H1(View itemView) {
            super(itemView);


            mName=(TextView)itemView.findViewById(R.id.t_packets_item_app_name);
            mNum=(TextView)itemView.findViewById(R.id.t_packets_item_ip);
            mIcon=(ImageView)itemView.findViewById(R.id.img_packets_item_icon);
            mArrow=(ImageView)itemView.findViewById(R.id.img_packets_item_arrow);
        }
    }

    static class H2 extends RecyclerView.ViewHolder
    {
        TextView text;

        public H2(View itemView) {
            super(itemView);
            text=(TextView)itemView.findViewById(R.id.t_packet_item_text);
        }
    }
}
