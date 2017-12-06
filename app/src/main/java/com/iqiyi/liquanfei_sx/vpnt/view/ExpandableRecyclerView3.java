package com.iqiyi.liquanfei_sx.vpnt.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.iqiyi.liquanfei_sx.vpnt.tools.LinkedNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/6.
 *
 */

public class ExpandableRecyclerView3 extends RecyclerView {

    private Adapter mAdapter=null;
    private MAdapter mInnerAdapter=null;
    private boolean mEnableAnimation=true;
    private ExpandableItem mRoot=new ExpandableItem(-1,null);

    private List<int[]> mCachedPosition=new ArrayList<>();


    /**
     * TODO when notify
     */
    private AdapterDataObserver mObserver=new AdapterDataObserver() {
        @Override
        public void onChanged() {
            //rangeChanged();
            mInnerAdapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mInnerAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mInnerAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            //itemInserted(positionStart,itemCount);
            mInnerAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mInnerAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mInnerAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    };

    public ExpandableRecyclerView3(Context context) {
        this(context,null,0);
    }

    public ExpandableRecyclerView3(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ExpandableRecyclerView3(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init()
    {
        setLayoutManager(new LinearLayoutManager(getContext()));

        if (!mEnableAnimation)
        {
            ((SimpleItemAnimator)getItemAnimator()).setSupportsChangeAnimations(false);
            getItemAnimator().setRemoveDuration(0);
            getItemAnimator().setMoveDuration(0);
            getItemAnimator().setChangeDuration(0);
            getItemAnimator().setAddDuration(0);
        }

        mInnerAdapter=new MAdapter();

        //TODO a item decoration
        addItemDecoration(new ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, 0, 0, 1);
            }
        });
        //mRealPosition.put(null,0);
        super.setAdapter(mInnerAdapter);
    }

    private class MAdapter extends RecyclerView.Adapter
    {
        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
            if (mAdapter!=null)
                mAdapter.onDetachedFromRecyclerView(recyclerView);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            if (mAdapter!=null)
                mAdapter.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onViewDetachedFromWindow(ViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            if (mAdapter!=null)
                mAdapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (mAdapter!=null)
                return mAdapter.onCreateViewHolder(ExpandableRecyclerView3.this,viewType);

            return null;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (mAdapter==null)
                return;
        }

        @Override
        public int getItemCount() {
            return mRoot.mSize;
        }

        @Override
        public int getItemViewType(int position) {
            if (mAdapter==null)
                return 0;


            return 0;
        }


        class ListenerInfo
        {
            /**
             * for listening the click action we must save the OnClickListener
             * that user sets
             */
            OnClickListener mL;


            //this ViewHolder is for purpose of getting item's real position.
            ViewHolder mHolder;

            ListenerInfo(OnClickListener l,ViewHolder h)
            {
                this.mHolder=h;
                this.mL =l;
            }
        }
    }

    public static abstract class Adapter extends RecyclerView.Adapter
    {
        private ExpandItemAddObserver mObserver;

        private void setObserver(ExpandItemAddObserver o)
        {
            mObserver=o;
        }

        public abstract void onBindViewHolder(ViewHolder holder,int []position);

        public abstract int getItemViewType(int []position);

        public abstract boolean canExpand(int []position);

        public abstract void onExpand(int []position);

        private int getItemCount(int []position)
        {
            return getItemCount(position,position.length);
        }

        public abstract int getItemCount(int[] position,int depth);

        @Override
        public final void onBindViewHolder(ViewHolder holder, int position) {
            onBindViewHolder(holder,new int[]{position});
        }

        @Override
        public final int getItemCount() {
            return getItemCount(new int[0],0);
        }

        public final void notifyItemAdd(int ...position)
        {
            mObserver.onAdd(position);
        }
    }

    private class ExpandableItem
    {
        int mIndex=0;
        int mSize=0;
        int mDepth;
        int mChildCount=0;
        ExpandableItem mParent;
        int mStartPosition=0,mEndPosition=0,mChildPosition=0;
        private LinkedNode<ExpandableItem> mChild,mStart,mEnd;
        private SparseArray<LinkedNode<ExpandableItem>> mExpands;

        ExpandableItem(int depth,ExpandableItem parent)
        {
            mParent=parent;
            mDepth=depth;
        }

        int[] get(int position)
        {
            if (mChild==null)
            {
                mEnd=mStart=mChild=new LinkedNode<>(new ExpandableItem(mDepth+1,this));
            }

            if (mChildPosition==position)
            {
                //当前child就是目标
                return saveChildPosition();
            }

            if (mChildPosition<position)
            {
                //向后get

                while(mChildPosition!=position)
                {
                    if (mChild.o.mSize!=0)
                    {
                        //当前child被展开，检查position处item是否位于child展开部分中
                        if (mChildPosition+mChild.o.mSize>=position)
                        {
                            //先保存child位置
                            saveChildPosition();
                            return mChild.o.get(position);
                        }
                    }

                    mChildPosition+=1+mChild.o.mSize;

                    if (mChild.next==null)
                    {
                        //添加下一个
                        if (mChildCount==20)
                        {
                            //如果个数满了，把第一个链接到最后一个成环
                            mChild.linkThisBefore(mStart);
                        }else
                        {
                            //new
                            mChildCount++;
                            mEnd=mChild.linkThisBefore(new LinkedNode<>(new ExpandableItem(mDepth+1,this)));
                        }
                    }else
                    {
                        /* next很有可能是之前被使用过的，更新它的数值
                        * 而且应该避开去更新一个被展开的child，反之应该去除并保存起来
                        * 插入一个新的
                        */
                        LinkedNode<ExpandableItem> ei;
                        if ((ei=mExpands.get(mChild.o.mIndex+1))!=null)
                        {
                            //下一个应该是被展开过得,取出保存的，替换
                            mChild.replaceThisNext(ei);
                        }else
                        {
                            //下一个不是被展开的
                            //如果旧的被展开则插入新的
                            if (mChild.next.o.mSize!=0)
                            {
                                mChild.replaceThisNext(new LinkedNode<>(new ExpandableItem(mDepth+1,this)));
                            }else
                            {
                                //否则更新
                                mChild.next.o.mIndex=mChild.o.mIndex+1;
                            }
                        }
                    }

                    mChild=mChild.next;
                }
                //循环退出后说明当前child已经是目标
                return saveChildPosition();
            }else
            {
                while(mChildPosition!=position)
                {
                    if (mChild.previous==null)
                    {
                        //添加上一个
                        if (mChildCount==20)
                        {
                            //如果个数满了，把最后一个链接到第一个成环
                            mChild.linkThisAfter(mEnd);
                        }else
                        {
                            //new
                            mChildCount++;
                            mStart=mChild.linkThisAfter(new LinkedNode<>(new ExpandableItem(mDepth+1,this)));
                        }

                        mChildPosition--;
                    }else
                    {
                        /* previous很有可能是之前被使用过的，更新它的数值
                        * 而且应该避开去更新一个被展开的child，反之应该去除并保存起来
                        * 插入一个新的
                        */
                        LinkedNode<ExpandableItem> ei;
                        if ((ei=mExpands.get(mChild.o.mIndex-1))!=null)
                        {
                            //上一个应该是被展开过得,取出保存的，替换
                            mChild.replaceThisPrevious(ei);
                        }else
                        {
                            //上一个不是被展开的
                            //如果旧的被展开则插入新的
                            if (mChild.next.o.mSize!=0)
                            {
                                mChild.replaceThisPrevious(new LinkedNode<>(new ExpandableItem(mDepth+1,this)));
                            }else
                            {
                                //否则更新
                                mChild.next.o.mIndex=mChild.o.mIndex-1;
                            }
                        }

                        mChildPosition-=mChild.previous.o.mSize+1;

                        mChild=mChild.previous;
                        if (mChild.o.mSize!=0)
                        {
                            //上一child被展开，检查position处item是否位于上一child展开部分中
                            if (mChildPosition<position)
                            {
                                //先保存child位置
                                saveChildPosition();
                                return mChild.o.get(position);
                            }else if (mChildPosition==position)
                            {
                                return saveChildPosition();
                            }
                        }
                    }
                }
            }
            return null;
        }

        private int[] saveChildPosition()
        {
            if (mDepth==-1)
            {
                int []r=mCachedPosition.get(0);
                r[0]=mChild.o.mIndex;
                return r;
            }else
            {
                int[] r=mCachedPosition.get(mDepth);
                int []r1=mCachedPosition.get(mChild.o.mDepth);
                System.arraycopy(r,0,r1,0,r.length);
                r1[r.length]=mChild.o.mIndex;
                return r1;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private class ExpandItemAddObserver
    {
        void onAdd(int ...position) {
        }
    }
}
