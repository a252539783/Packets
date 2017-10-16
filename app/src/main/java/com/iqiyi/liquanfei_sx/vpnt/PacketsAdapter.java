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

/**
 * Created by Administrator on 2017/10/9.
 */

public class PacketsAdapter extends ExpandableRecyclerView.Adapter<PacketsAdapter.H1> {
    private ArrayList<ServerService.PacketList> mPacketLists =null;
    private LayoutInflater mLf=null;

    public PacketsAdapter(ArrayList<ServerService.PacketList> packets,Context c)
    {
        mPacketLists =packets;
        mLf=LayoutInflater.from(c);
    }

    @Override
    public void onBindExpandView(ExpandableRecyclerView view, int position) {
        view.setAdapter(new ChildAdapter(position));
    }

    @Override
    public boolean canExpand(int position) {
        return mPacketLists.size()!=0;
        //return false;
    }

    @Override
    public PacketsAdapter.H1 onCreateViewHolder(ViewGroup parent, int viewType) {
        return new H1(mLf.inflate(R.layout.item_packets,parent,false));
    }

    @Override
    public void onBindViewHolder(PacketsAdapter.H1 holder, int position) {
        if (position==mPacketLists.size())
            return ;
        ServerService.PacketList packetList= mPacketLists.get(position);
        AppPortList.AppInfo info=packetList.info();
        holder.icon.setImageDrawable(info.icon);
        holder.name.setText(info.appName+":"+info.info.applicationInfo.packageName);
        holder.ip.setText(packetList.ip()+":"+packetList.mSPort);
    }

    @Override
    public int getItemCount() {
        return mPacketLists.size();
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
            TCPPacket packet=mPacketLists.get(mPosition).get(position);
            holder.text.setText(mPacketLists.get(mPosition).get(position).getIpInfo().getHeader());
        }

        @Override
        public int getItemCount() {
            return mPacketLists.get(mPosition).size();
        }
    }
}
