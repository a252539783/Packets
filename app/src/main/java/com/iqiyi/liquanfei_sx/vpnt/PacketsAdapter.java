package com.iqiyi.liquanfei_sx.vpnt;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iqiyi.liquanfei_sx.vpnt.packet.TCPPacket;
import com.iqiyi.liquanfei_sx.vpnt.service.ServerService;
import com.iqiyi.liquanfei_sx.vpnt.tools.AppPortList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/9.
 */

public class PacketsAdapter extends ExpandableRecyclerView.Adapter<PacketsAdapter.H1> {

    public static final int FILTER_NO=0;
    public static final int FILTER_IP=0x1;
    public static final int FILTER_PORT=0x2;
    public static final int FILTER_APP=0x4;
    public static final int FILTER_PKG=0x8;

    private ArrayList<ServerService.PacketList> mPacketLists =null;
    private LayoutInflater mLf=null;

    private List<Integer> mNo=new ArrayList<>(),mIpFilter,mPortFilter,mAppFilter,mPkgFilter,mCurrent=mNo;
    private int mFilterType=FILTER_NO;
    private String mIpFilterKey,mAppFilterKey,mPkgFilterKey;
    private int mPortFilterKey;
//
//    private RecyclerView.AdapterDataObserver mObserver=new RecyclerView.AdapterDataObserver() {
//        @Override
//        public void onChanged() {
//            filterAll();
//        }
//
//        @Override
//        public void onItemRangeChanged(int positionStart, int itemCount) {
//            filterAll();
//        }
//
//        @Override
//        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
//            filterAll();
//        }
//
//        @Override
//        public void onItemRangeInserted(int positionStart, int itemCount) {
//            filterAdd();
//        }
//
//        @Override
//        public void onItemRangeRemoved(int positionStart, int itemCount) {
//            filterAll();
//        }
//
//        @Override
//        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
//            filterAll();
//        }
//    };

    public PacketsAdapter(ArrayList<ServerService.PacketList> packets,Context c)
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
        ServerService.PacketList pl=mPacketLists.get(mPacketLists.size()-1);
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

    public void setFilterKey(String key,int type)
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
    public PacketsAdapter.H1 onCreateViewHolder(ViewGroup parent, int viewType) {
        return new H1(mLf.inflate(R.layout.item_packets,parent,false));
    }

    @Override
    public void onBindViewHolder(PacketsAdapter.H1 holder, int position) {
        ServerService.PacketList packetList= mPacketLists.get(mCurrent.get(position));
        AppPortList.AppInfo info=packetList.info();
        holder.icon.setImageDrawable(info.icon);
        holder.name.setText(info.appName+":"+info.info.applicationInfo.packageName);
        holder.ip.setText(packetList.ip()+":"+packetList.mSPort);
    }

    @Override
    public int getItemCount() {
        return mCurrent.size();
    }

    static class H1 extends RecyclerView.ViewHolder
    {
        TextView name,ip;
        ImageView icon,arrow;

        public H1(View itemView) {
            super(itemView);

            name=(TextView)itemView.findViewById(R.id.t_packets_item_app_name);
            ip=(TextView)itemView.findViewById(R.id.t_packets_item_ip);
            icon=(ImageView)itemView.findViewById(R.id.img_packets_item_icon);
            arrow=(ImageView)itemView.findViewById(R.id.img_packets_item_arrow);
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

    private class ChildAdapter extends ExpandableRecyclerView.Adapter<H2>
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
        public H2 onCreateViewHolder(ViewGroup parent, int viewType) {
            return new H2(mLf.inflate(R.layout.item_packet,parent,false));
        }

        @Override
        public void onBindViewHolder(H2 holder, int position) {
            TCPPacket packet=mPacketLists.get(mCurrent.get(mPosition)).get(position);
            holder.text.setText(mPacketLists.get(mPosition).get(position).getIpInfo().getHeader());//+new String(mPacketLists.get(mPosition).get(position).getRawData(),mPacketLists.get(mPosition).get(position).mOffset,mPacketLists.get(mPosition).get(position).getDataLength()
        }

        @Override
        public int getItemCount() {
            return mPacketLists.get(mCurrent.get(mPosition)).size();
        }
    }
}
