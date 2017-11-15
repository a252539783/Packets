package com.iqiyi.liquanfei_sx.vpnt.editor;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2017/11/15.
 */

public class EditPaketInfo implements Parcelable {

    int mHistory;
    int mListIndex;
    int mIndex;

    protected EditPaketInfo(Parcel in) {
        this(in.readInt(),in.readInt(),in.readInt());
    }

    public EditPaketInfo(int history,int list,int index)
    {
        mHistory=history;
        mListIndex=list;
        mIndex=index;
    }

    public static final Creator<EditPaketInfo> CREATOR = new Creator<EditPaketInfo>() {
        @Override
        public EditPaketInfo createFromParcel(Parcel in) {
            return new EditPaketInfo(in);
        }

        @Override
        public EditPaketInfo[] newArray(int size) {
            return new EditPaketInfo[size];
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
