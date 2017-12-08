package com.iqiyi.liquanfei_sx.vpnt.history;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iqiyi.liquanfei_sx.vpnt.IAdapter;
import com.iqiyi.liquanfei_sx.vpnt.R;
import com.iqiyi.liquanfei_sx.vpnt.editor.EditActivity;
import com.iqiyi.liquanfei_sx.vpnt.editor.EditPacketInfo;
import com.iqiyi.liquanfei_sx.vpnt.packet.LocalPackets;
import com.iqiyi.liquanfei_sx.vpnt.packet.PacketList;
import com.iqiyi.liquanfei_sx.vpnt.packet.PersistRequest;
import com.iqiyi.liquanfei_sx.vpnt.packet.TCPPacket;
import com.iqiyi.liquanfei_sx.vpnt.tools.AppPortList;
import com.iqiyi.liquanfei_sx.vpnt.view.ExpandableRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/9.
 */


public class HistoryAdapter extends ExpandableRecyclerView.Adapter<HistoryAdapter.H1> implements LocalPackets.OnHistoryChangeListener,IAdapter
{
    private List<LocalPackets.CaptureInfo> mAllHistory=null;
    private LayoutInflater mLf;
    private Context mContext;

    //private SparseArray<ExpandableRecyclerView.Adapter> mAdapters;

    public HistoryAdapter(Context c)
    {
        mContext=c;
        mLf=LayoutInflater.from(c);
    }

    @Override
    public void setSource(Object src)
    {
        mAllHistory= (List<LocalPackets.CaptureInfo>) src;
    }

    @Override
    public void removeListeners() {

    }

    @Override
    public void setListeners() {

    }

    @Override
    public void onFilterChanged() {

    }

    @Override
    public void onBindExpandView(ExpandableRecyclerView view, int position) {
//        if (mAdapters==null)
//            mAdapters=new SparseArray<>();

        final PacketsAdapter adapter=new PacketsAdapter(position);
        //mAdapters.put(position,adapter);
        view.setAdapter(adapter);
        LocalPackets.get().addPacketsChangeListener(adapter);
        LocalPackets.mgr().addRequest(PersistRequest.newReadRequest(position));
    }

    @Override
    public boolean canExpand(int position) {
        return true;
    }

    @Override
    public H1 onCreateViewHolder(ViewGroup parent, int viewType) {
        return new H1(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history,parent,false));
    }

    @Override
    public void onBindViewHolder(H1 holder, int position) {
        holder.date.setText(mAllHistory.get(position).mTime+"");
    }

    @Override
    public int getItemCount() {
        return mAllHistory==null?0:mAllHistory.size();
    }

    @Override
    public void onChange() {
        notifyDataSetChanged();
    }

    @Override
    public void onAdd(int timeIndex) {

    }

    public class PacketsAdapter extends ExpandableRecyclerView.Adapter<H2> implements LocalPackets.OnPacketsChangeListener{

        public static final int FILTER_NO=0;
        public static final int FILTER_IP=0x1;
        public static final int FILTER_PORT=0x2;
        public static final int FILTER_APP=0x4;
        public static final int FILTER_PKG=0x8;

        private List<PacketList> mPacketLists =null;

        private List<Integer> mNo=new ArrayList<>(),mIpFilter,mPortFilter,mAppFilter,mPkgFilter,mCurrent=mNo;
        private int mFilterType=FILTER_NO;
        private String mIpFilterKey,mAppFilterKey,mPkgFilterKey;
        private int mPortFilterKey;
        private int mTime;

        public PacketsAdapter(int time)
        {
            mPacketLists =mAllHistory.get(time).mPackets;
            freshFilter();
            mTime=time;
            //registerAdapterDataObserver(mObserver);
        }

        void filterAll()
        {
            filter(0,mPacketLists.size());
        }

        void filterAdd(int position)
        {
            PacketList pl=mPacketLists.get(position);
            if (mFilterType==FILTER_NO) {
                mCurrent.add(position);
                notifyItemInserted(position);
            }
            else
            {
                if (((mFilterType&FILTER_APP)!=0&&pl.info().appName.contains(mAppFilterKey))||
                        ((mFilterType&FILTER_PORT)!=0&&pl.port()==mPortFilterKey)||
                        ((mFilterType&FILTER_IP)!=0&&pl.ip().contains(mIpFilterKey))||
                        ((mFilterType&FILTER_PKG)!=0&&pl.info().info.packageName.contains(mPkgFilterKey)))
                {
                    mCurrent.add(position);
                    notifyItemInserted(position);
                }
            }
        }

        private void filter(int start,int end)
        {
            if (mCurrent.size()!=mPacketLists.size())
            {
                mNo.clear();
                for (int i=0;i<mPacketLists.size();i++)
                {
                    mNo.add(i);
                }
            }
            mCurrent=mNo;

            if ((mFilterType&FILTER_IP)!=0)
            {
                if (mIpFilter==null)
                {
                    mIpFilter=new ArrayList<>();
                }else
                {
                    mIpFilter.clear();
                }

                for (int i=start;i<end;i++)
                {
                    if (mPacketLists.get(mCurrent.get(i)).ip().contains(mIpFilterKey))
                        mIpFilter.add(mCurrent.get(i));
                }

                mCurrent=mIpFilter;
                start=0;
                end=mCurrent.size();
            }

            if ((mFilterType&FILTER_PORT)!=0)
            {
                if (mPortFilter==null)
                {
                    mPortFilter=new ArrayList<>();
                }else
                {
                    mPortFilter.clear();
                }

                for (int i=start;i<end;i++)
                {
                    if (mPacketLists.get(mCurrent.get(i)).port()==mPortFilterKey)
                        mPortFilter.add(mCurrent.get(i));
                }

                mCurrent=mPortFilter;
                start=0;
                end=mCurrent.size();
            }

            if ((mFilterType&FILTER_APP)!=0)
            {
                if (mAppFilter==null)
                {
                    mAppFilter=new ArrayList<>();
                }else
                {
                    mAppFilter.clear();
                }

                for (int i=start;i<end;i++)
                {
                    if (mPacketLists.get(mCurrent.get(i)).info().appName.contains(mAppFilterKey))
                        mAppFilter.add(mCurrent.get(i));
                }

                mCurrent=mAppFilter;
                start=0;
                end=mCurrent.size();
            }

            if ((mFilterType&FILTER_PKG)!=0)
            {
                if (mPkgFilter==null)
                {
                    mPkgFilter=new ArrayList<>();
                }else
                {
                    mPkgFilter.clear();
                }

                for (int i=start;i<end;i++)
                {
                    if (mPacketLists.get(mCurrent.get(i)).info().appName.contains(mPkgFilterKey))
                        mPkgFilter.add(mCurrent.get(i));
                }

                mCurrent=mPkgFilter;
                start=0;
                end=mCurrent.size();
            }
        }

        public void setFilterKey(int type,String key)
        {
            mFilterType=(mFilterType|type);
            switch (type)
            {
                case FILTER_APP:
                    mAppFilterKey=key;
                    break;
                case FILTER_IP:
                    mIpFilterKey=key;
                    break;
                case FILTER_PKG:
                    mPkgFilterKey=key;
                    break;
                case FILTER_PORT:
                    mPortFilterKey=Integer.parseInt(key);
                    break;
            }
        }

        public void clearFilterKey(int type)
        {
            if (type==FILTER_NO)
                mFilterType=type;
            else
                mFilterType=(mFilterType&(~type));
        }

        public void freshFilter()
        {
            filterAll();
            notifyDataSetChanged();
        }

        public void notifyDataInserted(int position)
        {
            filterAdd(position);
        }

        @Override
        public void onBindExpandView(ExpandableRecyclerView view, int position) {
            ChildAdapter ca=new ChildAdapter(position);
            view.setAdapter(ca);
            LocalPackets.get().addPacketChangeListener(ca);
            LocalPackets.mgr().addRequest(PersistRequest.newReadRequest(mTime,position));
        }

        @Override
        public boolean canExpand(int position) {
            return true;
        }

        @Override
        public H2 onCreateViewHolder(ViewGroup parent, int viewType) {
            return new H2(mLf.inflate(R.layout.item_packets,parent,false));
        }

        @Override
        public void onBindViewHolder(H2 holder, int position) {
            PacketList packetList= mPacketLists.get(mCurrent.get(position));
            AppPortList.AppInfo info=packetList.info();
            if (info!=null) {
                holder.icon.setImageDrawable(info.icon);
                holder.name.setText(info.appName + ":" + info.info.applicationInfo.packageName);
            }
            holder.ip.setText(packetList.ip()+":"+packetList.mSPort);
        }

        @Override
        public int getItemCount() {
            return mCurrent.size();
        }


        @Override
        public void onChange(int time) {
            if (time==mTime)
                freshFilter();
        }

        @Override
        public void onAdd(int time, int index) {
            if (time==mTime)
                notifyDataInserted(index);
        }

        private class ChildAdapter extends ExpandableRecyclerView.Adapter<H3> implements LocalPackets.OnPacketChangeListener,View.OnClickListener
        {

            int mPosition;

            ChildAdapter(int position)
            {
                mPosition=position;
            }

            @Override
            public void onBindExpandView(ExpandableRecyclerView view, int position) {

            }

            @Override
            public boolean canExpand(int position) {
                return false;
            }

            @Override
            public H3 onCreateViewHolder(ViewGroup parent, int viewType) {
                H3 h= new H3(mLf.inflate(R.layout.item_packet,parent,false));
                h.itemView.setOnClickListener(this);
                Log.e("xx","create h3");
                return h;
            }

            @Override
            public void onBindViewHolder(H3 holder, int position) {
                TCPPacket packet=mPacketLists.get(mCurrent.get(mPosition)).get(position).mPacket;
                holder.text.setText(packet.getIpInfo().getHeader()+new String(packet.getRawData(),packet.mOffset,packet.getDataLength()));
                holder.itemView.setId(position);
            }

            @Override
            public int getItemCount() {
                return mPacketLists.get(mCurrent.get(mPosition)).size();
            }

            @Override
            public void onChange(int time, int index) {
//                if (time==mTime&&index==mPosition)
//                    notifyDataSetChanged();
            }

            @Override
            public void onAdd(int time, int listIndex, int index) {
                if (time==mTime&&listIndex==mPosition)
                    notifyItemInserted(index);
            }

            @Override
            public void onClick(View view) {
                edit(mTime,mPosition,view.getId());
            }
        }
    }

    static class H1 extends RecyclerView.ViewHolder
    {
        TextView date;
        public H1(View itemView) {
            super(itemView);

            date=(TextView)itemView.findViewById(R.id.text_history);
        }
    }

    static class H2 extends RecyclerView.ViewHolder
    {
        TextView name,ip;
        ImageView icon,arrow;

        public H2(View itemView) {
            super(itemView);

            name=(TextView)itemView.findViewById(R.id.t_packets_item_app_name);
            ip=(TextView)itemView.findViewById(R.id.t_packets_item_ip);
            icon=(ImageView)itemView.findViewById(R.id.img_packets_item_icon);
            arrow=(ImageView)itemView.findViewById(R.id.img_packets_item_arrow);
        }
    }

    static class H3 extends RecyclerView.ViewHolder
    {
        TextView text;

        public H3(View itemView) {
            super(itemView);
            text=(TextView)itemView.findViewById(R.id.t_packet_item_text);
        }
    }


    public void edit(int history,int listIndex,int index)
    {
        EditPacketInfo pi=new EditPacketInfo(history,listIndex,index);
        edit(pi);
    }

    public void edit(EditPacketInfo pi)
    {
        Intent i=editIntent();
        i.setAction(EditActivity.ACTION_OPEN_PACKET);
        i.putExtra(EditActivity.ACTION_OPEN_PACKET,pi);
        mContext.startActivity(i);
    }

    private Intent editIntent()
    {
        return new Intent(mContext,EditActivity.class);
    }

}
