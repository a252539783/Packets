package com.lqf.packets.text;

import android.util.SparseArray;

/**
 * Created by Administrator on 2017/11/1.
 */

public class InputHistory {

    public static int INPUT_ADD=1;
    public static int INPUT_DELETE=3;

    private int mStart=0;
    private int mType=0;
    private byte[] mModified =null;

    public InputHistory(int start,byte []before,boolean f)
    {
        mStart=start;
        mModified=before;
        mType=INPUT_DELETE;
    }

    public InputHistory(int start,byte[] add)
    {
        mStart=start;
        mModified =add;
        mType=INPUT_ADD;
    }

    public static class HistoryManager
    {
        private HistoryManager mManager=new HistoryManager();

        private SparseArray<AddedInput> mAllHistory = new SparseArray<>();
        private int mLastIndex=-1;

        private HistoryManager()
        {
        }

        public void edit(int index)
        {
            if (mLastIndex==index)
                return ;

            mLastIndex=index;
            if (mAllHistory.get(mLastIndex)==null)
            {
                //mAllHistory.put(mLastIndex,new AddedInput());
            }
        }
    }

}
