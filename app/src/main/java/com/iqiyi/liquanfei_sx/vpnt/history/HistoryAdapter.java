package com.iqiyi.liquanfei_sx.vpnt.history;

import android.content.Context;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iqiyi.liquanfei_sx.vpnt.R;
import com.iqiyi.liquanfei_sx.vpnt.packet.LocalPackets;
import com.iqiyi.liquanfei_sx.vpnt.packet.PersistRequest;
import com.iqiyi.liquanfei_sx.vpnt.packet.TCPPacket;
import com.iqiyi.liquanfei_sx.vpnt.tools.AppPortList;
import com.iqiyi.liquanfei_sx.vpnt.view.ExpandableRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/9.
 */


public class HistoryAdapter extends ExpandableRecyclerView.Adapter<HistoryAdapter.H1>
{
    private List<LocalPackets.CaptureInfo> mAllHistory=null;
    private H mh=new H();

    private SparseArray<ExpandableRecyclerView.Adapter> mAdapters;

    public HistoryAdapter()
    {
    }

    void setHistorySource(List<LocalPackets.CaptureInfo> src)
    {
        mAllHistory=src;
    }

    @Override
    public void onBindExpandView(ExpandableRecyclerView view, int position) {
        if (mAdapters==null)
            mAdapters=new SparseArray<>();

        final ExpandableRecyclerView.Adapter adapter=new PacketsAdapter(mAllHistory.get(position).mPackets,view.getContext());
        mAdapters.put(position,adapter);
        view.setAdapter(adapter);

//        LocalPackets.mgr().addRequest(PersistRequest.newReadRequest(new PersistRequest.OnLoadHistoryListener() {
//            @Override
//            public void loadOne(int index) {
//                Message msg=new Message();
//                msg.what=H.NOTIFY_PACKETS;
//                msg.arg1=index;
//                msg.obj=adapter;
//                mh.sendMessage(msg);
//            }
//
//            @Override
//            public void loadStart(List<LocalPackets.CaptureInfo> all) {
//            }
//        }, position));
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

    public class PacketsAdapter extends ExpandableRecyclerView.Adapter<H2> {

        public static final int FILTER_NO=0;
        public static final int FILTER_IP=0x1;
        public static final int FILTER_PORT=0x2;
        public static final int FILTER_APP=0x4;
        public static final int FILTER_PKG=0x8;

        private List<LocalPackets.PacketList> mPacketLists =null;
        private LayoutInflater mLf=null;

        private List<Integer> mNo=new ArrayList<>(),mIpFilter,mPortFilter,mAppFilter,mPkgFilter,mCurrent=mNo;
        private int mFilterType=FILTER_NO;
        private String mIpFilterKey,mAppFilterKey,mPkgFilterKey;
        private int mPortFilterKey;

        public PacketsAdapter(List<LocalPackets.PacketList> packets,Context c)
        {
            mPacketLists =packets;
            freshFilter();
            mLf=LayoutInflater.from(c);
            //registerAdapterDataObserver(mObserver);
        }

        void filterAll()
        {
            filter(0,mPacketLists.size());
        }

        void filterAdd(int position)
        {
            int oSize=mCurrent.size();
            LocalPackets.PacketList pl=mPacketLists.get(mPacketLists.size()-1);
            if (mFilterType==FILTER_NO) {
                mCurrent.add(mPacketLists.size() - 1);
                notifyItemInserted(mCurrent.size()-1);
            }
            else
            {
                if (((mFilterType&FILTER_APP)!=0&&pl.info().appName.contains(mAppFilterKey))||
                        ((mFilterType&FILTER_PORT)!=0&&pl.port()==mPortFilterKey)||
                        ((mFilterType&FILTER_IP)!=0&&pl.ip().contains(mIpFilterKey))||
                        ((mFilterType&FILTER_PKG)!=0&&pl.info().info.packageName.contains(mPkgFilterKey)))
                {
                    mCurrent.add(mPacketLists.size()-1);
                    notifyItemInserted(mCurrent.size()-1);
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
            view.setAdapter(new ChildAdapter(position));
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
            LocalPackets.PacketList packetList= mPacketLists.get(mCurrent.get(position));
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

        private class ChildAdapter extends ExpandableRecyclerView.Adapter<H3>
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
                return new H3(mLf.inflate(R.layout.item_packet,parent,false));
            }

            @Override
            public void onBindViewHolder(H3 holder, int position) {
                TCPPacket packet=mPacketLists.get(mCurrent.get(mPosition)).get(position).mPacket;
                holder.text.setText(mPacketLists.get(mCurrent.get(mPosition)).get(position).mPacket.getIpInfo().getHeader());//+new String(mPacketLists.mgr(mPosition).mgr(position).getRawData(),mPacketLists.mgr(mPosition).mgr(position).mOffset,mPacketLists.mgr(mPosition).mgr(position).getDataLength()
            }

            @Override
            public int getItemCount() {
                return mPacketLists.get(mCurrent.get(mPosition)).size();
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

    static class H extends android.os.Handler
    {
        static final int NOTIFY_PACKETS=0;
        static final int NOTIFY_PACKET=1;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what)
            {
                case NOTIFY_PACKETS:
                case NOTIFY_PACKET:
                    ((PacketsAdapter)msg.obj).notifyDataInserted(msg.arg1);
                    break;
            }
        }
    }
}
