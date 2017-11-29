package com.iqiyi.liquanfei_sx.vpnt.editor;

import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.iqiyi.liquanfei_sx.vpnt.CommonPresenter;
import com.iqiyi.liquanfei_sx.vpnt.R;
import com.iqiyi.liquanfei_sx.vpnt.packet.LocalPackets;
import com.iqiyi.liquanfei_sx.vpnt.packet.PacketList;
import com.iqiyi.liquanfei_sx.vpnt.packet.PersistRequest;
import com.iqiyi.liquanfei_sx.vpnt.packet.TCPPacket;
import com.iqiyi.liquanfei_sx.vpnt.text.AddedInput;
import com.iqiyi.liquanfei_sx.vpnt.text.FileInfo;
import com.iqiyi.liquanfei_sx.vpnt.view.FixedWidthTextView;

import java.io.File;
import java.io.IOException;

import static com.iqiyi.liquanfei_sx.vpnt.editor.EditActivity.ACTION_OPEN_PACKET;

/**
 * Created by Administrator on 2017/11/15.
 */

public class EditPresenter extends CommonPresenter implements Toolbar.OnMenuItemClickListener{

    FixedWidthTextView mTextView;
    EditActivity mActivity;
    Toolbar mToolbar;

    private TCPPacket mPacket=null;
    private PacketList mList=null;

    EditPresenter(EditActivity activity)
    {
        this.mActivity=activity;
        AddedInput.CacheThread.init();
    }

    @Override
    protected void onViewBind(View v) {
        mTextView=(FixedWidthTextView)v.findViewById(R.id.text_test);
        mToolbar=(Toolbar)v.findViewById(R.id.tool_bar);
        mActivity.setSupportActionBar(mToolbar);
        mToolbar.setOnMenuItemClickListener(this);

        String action=mActivity.getIntent().getAction();
        Intent i=mActivity.getIntent();
        if (action.equals(ACTION_OPEN_PACKET))
        {
            EditPacketInfo pi=i.getParcelableExtra(ACTION_OPEN_PACKET);
            mList=LocalPackets.get().mAllPackets.get(pi.mHistory).mPackets.get(pi.mListIndex);
            mPacket=mList.get(pi.mIndex).mPacket;
            mTextView.setBytes(mPacket.getRawData());
        }else if (action.equals(Intent.ACTION_VIEW))
        {
            try {
                mTextView.setFile(new FileInfo(new File(mActivity.getIntent().getData().getPath())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_b_save:
                    if (mPacket!=null)
                    {
                        if (!LocalPackets.get().containSaved(mList.info().info.applicationInfo.uid))
                        {
                            LocalPackets.get().newSaved(mList.info().info.applicationInfo.uid);
                        }

                        LocalPackets.mgr().addRequest(PersistRequest.newWriteSavedRequest("",System.nanoTime(),mList,mPacket));
                    }
                break;
        }

        return false;
    }
}
