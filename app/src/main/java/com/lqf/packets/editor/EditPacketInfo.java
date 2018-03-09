package com.lqf.packets.editor;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2017/11/15.
 */

public class EditPacketInfo implements Parcelable {

    int mHistory;
    int mListIndex;
    int mIndex;

    protected EditPacketInfo(Parcel in) {
        this(in.readInt(),in.readInt(),in.readInt());
    }

    public EditPacketInfo(int history, int list, int index)
    {
        mHistory=history;
        mListIndex=list;
        mIndex=index;
    }

    public static final Creator<EditPacketInfo> CREATOR = new Creator<EditPacketInfo>() {
        @Override
        public EditPacketInfo createFromParcel(Parcel in) {
            return new EditPacketInfo(in);
        }

        @Override
        public EditPacketInfo[] newArray(int size) {
            return new EditPacketInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mHistory);
        dest.writeInt(mListIndex);
        dest.writeInt(mIndex);
    }
}
