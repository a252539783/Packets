package com.lqf.packets.history;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lqf.packets.IAdapter;
import com.lqf.packets.R;
import com.lqf.packets.editor.EditActivity;
import com.lqf.packets.editor.EditPacketInfo;
import com.lqf.packets.packet.ClientService;
import com.lqf.packets.packet.LocalPackets;
import com.lqf.packets.packet.PacketList;
import com.lqf.packets.packet.PersistRequest;
import com.lqf.packets.packet.TCPPacket;
import com.lqf.packets.tools.AppPortList;
import com.lqf.packets.view.ExpandableRecyclerView3;

import java.util.List;

/**
 * Created by Administrator on 2017/11/27.
 */

public class HistoryAdapter2 extends ExpandableRecyclerView3.Adapter implements IAdapter,LocalPackets.OnHistoryChangeListener,LocalPackets.OnPacketsChangeListener,LocalPackets.OnPacketChangeListener,View.OnClickListener{

    private final static int ITEM_TYPE_HISTORY=1;
    private final static int ITEM_TYPE_PACKETS=2;
    private final static int ITEM_TYPE_PACKET=3;

    private Handler mLoadHandler=null;

    private Thread mLoadThread=new LoadThread();

    private List<LocalPackets.CaptureInfo> mAllHistory=null;
    private LayoutInflater mLf;
    private Context mContext;

    //private SparseArray<ExpandableRecyclerView.Adapter> mAdapters;

    public HistoryAdapter2(Context c)
    {
        mContext=c;
        mLf=LayoutInflater.from(c);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void removeListeners()
    {
        LocalPackets.get().removePacketsListener(this);
        LocalPackets.get().removeHistoryListener(this);
    }

    @Override
    public void setListeners() {
        LocalPackets.get().addPacketsChangeListener(this);
        LocalPackets.get().addPacketChangeListener(this);
    }

    @Override
    public void onFilterChanged() {
        notifyFresh(1);
        for (int i=0;i<mAllHistory.size();i++)
            LocalPackets.mgr().addRequest(PersistRequest.newReadRequest(i));
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void setSource(Object src)
    {
        mAllHistory= (List<LocalPackets.CaptureInfo>) src;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int[] position) {
        if (holder instanceof H1)
        {
            bindHistory((H1)holder,position[0]);
        }else if (holder instanceof H2)
        {
            bindPackets((H2)holder,position[0],position[1]);
        }else if (holder instanceof H3)
        {
            bindPacket((H3)holder,position[0],position[1],position[2]);
        }else
        {
            Log.e("HistoryAdapter","Exception:onBindViewHolder for a unsupported depth:"+position.length);
        }
    }

    @Override
    public int getItemViewType(int[] position) {
        return position.length;
    }

    @Override
    public boolean canExpand(int[] position) {
        switch (position.length)
        {
            case 1:
                return true;
            case 2:
                return true;
            case 3:
                return false;
        }

        Log.e("HistoryAdapter","Exception:canExpand for a unsupported depth");
        return false;
    }

    @Override
    public void onExpand(int[] position) {
        switch (position.length)
        {
            case 1:
                LocalPackets.mgr().addRequest(PersistRequest.newReadRequest(position[0]));
                break;
            case 2:
                LocalPackets.mgr().addRequest(PersistRequest.newReadRequest(position[0],position[1]));
                break;
        }
    }

    @Override
    public int getItemCount(int[] position, int depth) {
        switch (depth)
        {
            case 0:
                return mAllHistory.size();
            case 1:
                return mAllHistory.get(position[0]).mPackets.size();
            case 2:
                return mAllHistory.get(position[0]).mPackets.get(position[1]).size();
        }

        Log.e("HistoryAdapter","Exception:getItemCount for a unsupported depth");
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType)
        {
            case ITEM_TYPE_HISTORY:
                return new H1(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history,parent,false));
            case ITEM_TYPE_PACKETS:
                return new H2(mLf.inflate(R.layout.item_packets,parent,false));
            case ITEM_TYPE_PACKET:
                H3 h= new H3(mLf.inflate(R.layout.item_packet,parent,false));
                //h.itemView.setOnClickListener(this);
                return h;
        }

        Log.e("HistoryAdapter","Exception:create unknown holder");
        return new H1(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history,parent,false));
    }

    private void bindHistory(H1 h,int position)
    {
        h.date.setText(mAllHistory.get(position).mTime+"");
    }

    private void bindPackets(H2 h,int historyIndex,int index)
    {
        PacketList packetList= mAllHistory.get(historyIndex).mPackets.get(index);
        AppPortList.AppInfo info=packetList.info();
        if (packetList.mAlive)
        {
            h.name.setTextColor(Color.BLACK);
        }else
        {
            h.name.setTextColor(Color.GRAY);
        }

        if (info!=null) {
            h.icon.setImageDrawable(info.icon);
            h.name.setText(info.appName + ":" + info.info.applicationInfo.packageName);
        }
        h.ip.setText(packetList.ip()+":"+packetList.mSPort);
    }

    private void bindPacket(H3 h,int historyIndex,int listIndex,int index)
    {
        TCPPacket packet= mAllHistory.get(historyIndex).mPackets.get(listIndex).get(index).mPacket;
        if (packet.getSourceIp().equals(ClientService.addr))
        {
            h.text.setTextColor(Color.BLACK);
        }else
        {
            h.text.setTextColor(Color.BLUE);
        }
        h.text.setText(new String(packet.getRawData(), packet.mOffset + packet.mHeaderLength, packet.getDataLength()));//packet.getIpInfo().getHeader());//+new String(packet.getRawData(),packet.mOffset,packet.getDataLength()));
        h.itemView.setTag(new EditPacketInfo(historyIndex,listIndex,index));
        h.itemView.setOnClickListener(this);
    }

    @Override
    public void onChange() {
        notifyDataSetChanged();
    }

    @Override
    public void onAdd(int timeIndex) {

    }

    @Override
    public void onChange(int time) {

    }

    @Override
    public void onAdd(int time, int index) {
        notifyItemAdd(time,index);
    }

    @Override
    public void onChange(int time, int index) {

    }

    @Override
    public void onAdd(int time, int listIndex, int index) {
        notifyItemAdd(time,listIndex,index);
    }

    @Override
    public void onClick(View v) {
        edit((EditPacketInfo) v.getTag());
    }

    public void edit(EditPacketInfo pi)
    {
        Intent i=editIntent();
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setAction(EditActivity.ACTION_OPEN_PACKET);
        i.putExtra(EditActivity.ACTION_OPEN_PACKET,pi);
        mContext.startActivity(i);
    }

    private Intent editIntent()
    {
        return new Intent(mContext,EditActivity.class);
    }

    static class H1 extends RecyclerView.ViewHolder
    {
        TextView date;
        public H1(View itemView) {
            super(itemView);

            date=(TextView)itemView.findViewById(R.id.text_history);
        }
    }

    public static class H2 extends RecyclerView.ViewHolder
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

    private static class LoadHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);


        }
    }

    private static class LoadThread extends Thread
    {
        LoadHandler mLoadHandler;

        @Override
        public void run() {
            super.run();
            mLoadHandler=new LoadHandler();
            Looper.prepare();
            Looper.loop();
        }
    }
}
