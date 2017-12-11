package com.iqiyi.liquanfei_sx.vpnt;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/12/4.
 */

public class FilterSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

    private String[] mKeys;

    private LayoutInflater mInflater;

    public FilterSpinnerAdapter(String[] keys)
    {
        mKeys=keys;
    }

    @Override
    public int getCount() {
        return mKeys.length;
    }

    @Override
    public Object getItem(int position) {
        return mKeys[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null)
        {
            if (mInflater==null)
            {
                mInflater=LayoutInflater.from(parent.getContext());
            }

            convertView=mInflater.inflate(R.layout.item_packet,parent,false);
            convertView.setBackgroundColor(Color.TRANSPARENT);
            ((TextView)convertView).setText(mKeys[position]);
        }

        return convertView;
    }
}
