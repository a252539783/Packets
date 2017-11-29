package com.iqiyi.liquanfei_sx.vpnt.saved;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iqiyi.liquanfei_sx.vpnt.IAdapter;
import com.iqiyi.liquanfei_sx.vpnt.R;
import com.iqiyi.liquanfei_sx.vpnt.packet.LocalPackets;
import com.iqiyi.liquanfei_sx.vpnt.packet.PersistRequest;
import com.iqiyi.liquanfei_sx.vpnt.packet.TCPPacket;
import com.iqiyi.liquanfei_sx.vpnt.tools.AppPortList;
import com.iqiyi.liquanfei_sx.vpnt.view.ExpandableRecyclerView2;

import java.util.List;

/**
 * Created by Administrator on 2017/11/29.
 */

public class SavedAdapter2 extends ExpandableRecyclerView2.Adapter implements IAdapter,LocalPackets.OnSavedItemChangeListener,View.OnClickListener {

    private static final int ITEM_SAVED=1;
    private static final int ITEM_PACKET=2;

    private List<LocalPackets.SavedInfo> mAllSaved =null;
    private LayoutInflater mLf;
    private Context mContext;

    public SavedAdapter2(Context c)
    {
        mContext=c;
        mLf=LayoutInflater.from(c);
    }

    @Override
    public void setSource(Object src) {
        mAllSaved= (List<LocalPackets.SavedInfo>) src;
    }

    @Override
    public void removeListeners() {
        LocalPackets.get().removeSavedItemListener(this);
    }

    @Override
    public void setListeners() {
        LocalPackets.get().addSavedItemChangeListener(this);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int[] position) {
        if (holder instanceof H1)
        {
            bindSaved((H1)holder,position[0]);
        }else if (holder instanceof H2)
        {
            bindPacket((H2)holder,position[0],position[1]);
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
                return false;
        }

        return false;
    }

    @Override
    public void onExpand(int[] position) {

        switch (position.length)
        {
            case 1:
                LocalPackets.mgr().addRequest(PersistRequest.newReadSavedRequest(mAllSaved.get(position[0]).mUid));
                break;
        }
    }

    @Override
    public int getItemCount(int[] position, int depth) {
        switch (depth)
        {
            case 0:
                return mAllSaved.size();
            case 1:
                return mAllSaved.get(position[0]).mPackets.size();
        }

        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType)
        {
            case ITEM_SAVED:
                return new H1(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_packets,parent,false));
            case ITEM_PACKET:
                return new H2(mLf.inflate(R.layout.item_packet,parent,false));
        }

        return new H1(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history,parent,false));
    }

    private void bindSaved(H1 holder,int position)
    {
        LocalPackets.SavedInfo si= mAllSaved.get(position);
        AppPortList.AppInfo info=si.mInfo;
        if (info!=null) {
            holder.mIcon.setImageDrawable(info.icon);
            holder.mName.setText(info.appName + ":" + info.info.applicationInfo.packageName);
        }
        holder.mNum.setText(si.mNum+"个");
    }

    private void bindPacket(H2 holder,int savedPosition,int position)
    {
        TCPPacket packet=mAllSaved.get(savedPosition).mPackets.get(position).mPackets.get(0).mPacket;
        holder.text.setText(packet.getIpInfo().getHeader());//+new String(packet.getRawData(),packet.mOffset,packet.getDataLength()));
        holder.itemView.setId(position);
    }

    @Override
    public void onChange(int time) {

    }

    @Override
    public void onAdd(int time, int index) {
        notifyItemAdd(time,index);
    }

    @Override
    public void onClick(View v) {

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
