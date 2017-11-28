package com.iqiyi.liquanfei_sx.vpnt.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.iqiyi.liquanfei_sx.vpnt.tools.Rf;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * 使用一个RecyclerView模拟出的折叠效果
 */

public class ExpandableRecyclerView2 extends RecyclerView implements View.OnClickListener{

    private Adapter mAdapter=null;

    private MAdapter mInnerAdapter=null;
    private ArrayMap<View,MAdapter.ListenerInfo> mChildClickListeners=new ArrayMap<>();

    private ExpandInfo mExpand=new ExpandInfo(null);

    private ExpandItemAddObserver mExpandObserver=new ExpandItemAddObserver();

    /**
     * TODO 外部notify的处理
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

    private boolean mCanMultiExpandable=true;

    private boolean mIsExpand=false;

    private int mDefaultDepth=3;

    private List<Boolean> mIsExpandView=new LinkedList<>();

    private ItemInfo mRoot=null,mStart=null,mEnd=null;
    private int mStartPosition=0,mEndPosition=0;
    private int mSize=0;

    public ExpandableRecyclerView2(Context context) {
        super(context);
        init();
    }

    public ExpandableRecyclerView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExpandableRecyclerView2(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private static OnClickListener getOnClickListener(View v)
    {
        Object li= Rf.readField(View.class,v,"mListenerInfo");
        if (li==null)
            return null;

        Object l=Rf.readField(li,"mOnClickListener");
        if (l==null)
            return null;

        return (OnClickListener)l;
    }

    private void init()
    {
        setLayoutManager(new LinearLayoutManager(getContext()));
        //getLayoutManager().setAutoMeasureEnabled(true);

//        ((SimpleItemAnimator)getItemAnimator()).setSupportsChangeAnimations(false);
//        getItemAnimator().setRemoveDuration(0);
//        getItemAnimator().setMoveDuration(0);
//        getItemAnimator().setChangeDuration(0);
//        getItemAnimator().setAddDuration(0);
//        setNestedScrollingEnabled(true);

        mInnerAdapter=new MAdapter();
        addItemDecoration(new ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, 0, 0, 1);
            }
        });
        super.setAdapter(mInnerAdapter);

        /**TODO
         * 为什么不能开启嵌套滚动呢。。。setNestedScrollingEnabled(true)
         */
        //setFocusableInTouchMode(false);//避免获取焦点、自动滚动
        //requestFocus();
    }

    public Adapter getAdapter()
    {
        return mAdapter;
    }

    public void setAdapter(Adapter adapter) {
        if (mAdapter==null||mAdapter!=adapter) {
            mAdapter = adapter;
            mAdapter.registerAdapterDataObserver(mObserver);
            mAdapter.setObserver(mExpandObserver);
        }
        //mAdapter.notifyDataSetChanged();
        mRoot=mStart=mEnd=null;
        mStartPosition=mEndPosition=0;
        mSize=mAdapter.getItemCount(new int[0],0);
        super.setAdapter(mInnerAdapter);
    }

    MAdapter innerAdapter()
    {
        return mInnerAdapter;
    }

    void expandItem(ItemInfo ii,int position)
    {
        if (position>=mSize) {
            Log.e("ExpandableRecyclerView","Exception: expand a item out of range");
            return;
        }

        ExpandInfo ei=mExpand.get(ii);
        if (ei==null)
        {
            expandItemUnchecked(ii,position);
        }else
        {
            collapseItemUnchecked(ii,position);
        }
    }

    private void collapseItemUnchecked(ItemInfo ii,int position)
    {

    }

    private void expandItemUnchecked(ItemInfo ii,int position)
    {
        ExpandInfo ei=mExpand.put(ii);

        int []index=contentPosition(ii,position);

        int size=mAdapter.getItemCount(index);
        if (size!=0)
        {
            ItemInfo last=ii.mNext;
            ItemInfo parent=ii;
            int depth=ii.mDepth+1;
            for (int i=0;i<size;i++)
            {
                ii.mNext=new ItemInfo(depth,i);
                ii.mNext.mPrevious=ii;
                ii.mNext.mParent=parent;
                ii=ii.mNext;
            }
            ii.mNext=last;
            if (last!=null) {
                last.mPrevious = ii;
            }
            if (parent==mEnd)
            {
                mEndPosition+=size;
                mEnd=ii;
            }else
            {
                mEndPosition+=size;
            }
            mInnerAdapter.notifyItemRangeInserted(position+1,size);

            mSize+=size;
            ei.mEnd=ii;

        }

        mAdapter.onExpand(index);
    }

    private void rangeChanged()
    {
        Iterator<Boolean> it=mIsExpandView.iterator();
        int i=0;
        while(it.hasNext())
        {
            if (it.next())
            {
                it.remove();
                mInnerAdapter.notifyItemRemoved(i);
            }else
            {
                i++;
            }
        }
        mIsExpandView.clear();
        for (i=0;i<mAdapter.getItemCount();i++)
        {
            mIsExpandView.add(false);
        }
    }

    private void itemInserted(int position,int len)
    {
        if (position>mAdapter.getItemCount())
            return ;

        ListIterator<Boolean> it=mIsExpandView.listIterator();
        Iterator<Boolean> it2=mIsExpandView.iterator();

        /**
         * 无意义的添加操作
         */
        int size=0;
        while (it2.hasNext())
        {
            if (!it2.next())
                size++;
        }
        if (size==mAdapter.getItemCount()||size<position)
            return ;

        while (it.hasNext())
        {
            if (position==0)
                break;

            if (!it.next())
            {
                position--;
            }
        }

        if (it.hasNext()&&!it.next())
        {
            it.previous();
        }

        for (;len!=0;len--)
        {
            it.add(false);
        }
    }

    private int []contentPosition(int position)
    {
        //要获取的一般都是在屏幕中显示的位置
        ItemInfo ii;
        if (position>=mStartPosition+mEndPosition/2)
        {
            //从后往前
            ii=mEnd;
            for(int index=mEndPosition;index!=position;index--)
            {
                ii=ii.mPrevious;
            }
        }else
        {
            //从前往后
            ii=mStart;
            for(int index=mStartPosition;index!=position;index++)
            {
                ii=ii.mNext;
            }
        }

        return contentPosition(ii,position);
    }

    /**
     * 保留计算位置时的数组
     */
    SparseArray<Position> mCachedPosition=null;
    private int[] contentPosition(ItemInfo item,int position)
    {
        int []rposition;
        if (mCachedPosition==null) {
            mCachedPosition = new SparseArray<>(mDefaultDepth);
        }

        Position cposition=mCachedPosition.get(item.mDepth);

        if (cposition==null){
            rposition=new int[item.mDepth+1];
            cposition=new Position(position,rposition);
            mCachedPosition.put(item.mDepth,cposition);
        }else
        {
            rposition=cposition.mPosition;
        }

        for (int i=rposition.length-1;i>=0;i--)
        {
            rposition[i]=item.mIndex;
            if (item.mParent==null)
                break;
            item=item.mParent;
        }

        return rposition;
    }

    private int[]  contentPosition(ItemInfo item)
    {
        int []rposition;
        rposition=new int[item.mDepth+1];

        for (int i=rposition.length-1;i>=0;i--)
        {
            rposition[i]=item.mIndex;
            if (item.mParent==null)
                break;
            item=item.mParent;
        }

        return rposition;
    }

    private ItemInfo preLoad(int position)
    {
        if (mRoot==null)
        {
            mRoot=new ItemInfo(0,0);
            mStart=mEnd=mRoot;
            return mRoot;
        }

        if (mStartPosition==position)
        {
            return mStart;
        }else
        if (mStartPosition>position)
        {
            /*
             * 向前绘制，不加载新的item
             */
            if (mStart.mPrevious==null)
            {
                /*
                 * 一般来说前面的都绘制过的，除非出bug，不会出现这种情况，
                 */
                Log.e("ExpandableRecyclerView","Exception:load previous null  position:"+position+" start:"+mStartPosition);
            }else
            {
                mStart=mStart.mPrevious;
                mEnd=mEnd.mPrevious;
                mStartPosition--;
                mEndPosition--;
                return mStart;
            }
        }else if (mEndPosition>position)
        {
            /*
              绘制的地方在开始和结束的中间，比如添加item时。
              此时从mEnd往回走进行处理(在添加时就进行过了加载)且不变动mEnd
             */

            ItemInfo end=mEnd;
            for (int i=mEndPosition;i!=position;i--)
            {
                end=end.mPrevious;
            }

            return end;
        }else if (mEndPosition<position)
        {
            if (mEnd.mNext==null)
            {
                //加载新的
                int[] index=contentPosition(mEnd,mEndPosition);
                ItemInfo parent=mEnd.mParent;
                for (int i=1;i<=index.length;i++)
                {
                    //向上遍历找到下一个item的位置
                    if (index[index.length-i]!=mAdapter.getItemCount(index,index.length-i)-1)
                    {
                        mEnd.mNext=new ItemInfo(i-1,index[index.length-i]+1);
                        mEnd.mNext.mPrevious=mEnd;
                        mEnd.mNext.mParent=parent;
                        break;
                    }
                    if (parent!=null)
                        parent=parent.mParent;
                }
            }

            mEnd=mEnd.mNext;
            mEndPosition++;
            /*
             * 让mStart与mEnd的距离始终保持一个屏幕所能容纳item的数量
             * （如果总数大于这个数值）
             */
            if (getChildCount()!=0)
            while (mEndPosition-mStartPosition>getChildCount()+2)
            {
                mStart=mStart.mNext;
                mStartPosition++;
            }
        }else if (mEndPosition==position)
        {
            //加载过的
        }

        return mEnd;
    }

    @Override
    public void onClick(View v) {
        MAdapter.ListenerInfo li=mChildClickListeners.get(v);
        expandItem(li.mii,li.mHolder.getAdapterPosition());

        OnClickListener l=li.mL;
        if (l!=null&&l!=this)
            l.onClick(v);
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

    /**
     * TODO 更多的回调
     */
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
            return mAdapter.onCreateViewHolder(ExpandableRecyclerView2.this,viewType);

            return null;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (mAdapter==null)
                return;

            ItemInfo ii=preLoad(position);
            int[] maskPosition=contentPosition(ii,position);

            mAdapter.onBindViewHolder(holder,maskPosition);
            if (mAdapter.canExpand(maskPosition))
            {
                View v=holder.itemView;
                OnClickListener l=getOnClickListener(v);
                MAdapter.ListenerInfo li=mChildClickListeners.get(v);
                if (li==null)
                {
                    mChildClickListeners.put(v,new MAdapter.ListenerInfo(ii,l,holder));
                    v.setOnClickListener(ExpandableRecyclerView2.this);
                }else
                {
                    if (l!=ExpandableRecyclerView2.this)
                    {
                        li.mL=l;
                    }
                    li.mii=ii;
                    li.mHolder=holder;
                }
            }
        }

        @Override
        public int getItemCount() {
            return mSize;
        }

        @Override
        public int getItemViewType(int position) {
            if (mAdapter==null)
                return 0;

            ItemInfo ii=preLoad(position);
            int []maskPosition=contentPosition(ii,position);
            return mAdapter.getItemViewType(maskPosition);
        }

        class ListenerInfo
        {
            OnClickListener mL;
            ItemInfo mii;
            ViewHolder mHolder;

            ListenerInfo(ItemInfo ii,OnClickListener l,ViewHolder h)
            {
                this.mHolder=h;
                this.mii=ii;
                this.mL =l;
            }
        }
    }

    private class ItemInfo implements Comparable<ItemInfo>
    {
        ItemInfo mNext=null,mPrevious=null,mParent=null;
        int mDepth,mIndex;

        ItemInfo(int depth,int index)
        {
            mDepth=depth;
            mIndex=index;
        }

        @Override
        public int compareTo(@NonNull ItemInfo ii) {
            if (ii==this)
                return 0;

            int []p1=contentPosition(ii);
            int []p2=contentPosition(this);

            for (int i=0;i<p1.length;i++)
            {
                if (p1[i]>p2[i]) {
                    return -1;
                }
                else if (p1[i]<p2[i]) {
                    return 1;
                }
            }
            if (p1.length==p2.length)
                return 0;

            return p2.length>p1.length?1:-1;
        }
    }

    private static class Position
    {
        /**
         * 此处mPosition中存放为某item的所有父节点位置以及item本身位置
         */
        int []mPosition;
        int mIntPosition;

        Position(int p,int[] po)
        {
            mPosition=po;
            mIntPosition=p;
        }
    }

    private static class ExpandInfo
    {
        SparseArray<ExpandInfo> mChildren;
        ItemInfo mItem,mEnd;

        ExpandInfo(ItemInfo item)
        {
            mItem=item;
            mEnd=null;
        }

        ExpandInfo get(int position)
        {
            return mChildren==null?null:mChildren.get(position);
        }

        ExpandInfo get(ItemInfo ii)
        {
            if (ii.mParent==mItem)
                return mChildren==null?null:mChildren.get(ii.mIndex);

            return get(ii.mParent).get(ii);
        }

        ExpandInfo put(ItemInfo ii)
        {
            if (mChildren==null)
                mChildren=new SparseArray<>();

            ExpandInfo ei;
            if (ii.mParent==mItem)
            {
                if ((ei=mChildren.get(ii.mIndex))!=null) {
                    return ei;
                }

                mChildren.put(ii.mIndex,ei=new ExpandInfo(ii));
                return ei;
            }

            if (ii.mParent!=null)
            {
                ei=put(ii.mParent);
            }else
            {
                /*
                不支持从低层ExpandInfo中添加高层ItemInfo
                 */
                return null;
            }

            return ei.put(ii);
        }
    }

    private class ExpandItemAddObserver
    {
        void onAdd(int ...position)
        {
            ExpandInfo ei=mExpand;
            int i=0;
            for (;i<position.length-1;i++)
            {
                ei=ei.get(position[i]);

                //通知的条目未被展开，不管它，等展开时自然加载
                if(ei==null)
                    return;
            }

            if (ei.mEnd==null)
            {
                ei.mEnd=new ItemInfo(ei.mItem.mDepth+1,position[i]);
                ei.mEnd.mNext=ei.mItem.mNext;
                ei.mItem.mNext=ei.mEnd;
                ei.mEnd.mPrevious=ei.mItem;
                ei.mEnd.mParent=ei.mItem;

                if (ei.mEnd.mNext!=null) {
                    ei.mEnd.mNext.mPrevious = ei.mEnd;
                }else
                {
                    //如果插入到了最后(mEnd刚好是最后一个才能触发)直接移动mEnd，其后进行更新
                    mEnd=mEnd.mNext;
                    mEndPosition++;
                }
            }else
            if (ei.mEnd.mIndex>=position[i])
            {
                //TODO 先忽略了mEnd之前的插入
            }else
            {
                //于ei.mEnd之后添加新的
                ItemInfo next=ei.mEnd.mNext;
                ei.mEnd.mNext=new ItemInfo(ei.mEnd.mDepth,position[i]);
                ei.mEnd.mNext.mNext=next;
                ei.mEnd.mNext.mPrevious=ei.mEnd;
                ei.mEnd=ei.mEnd.mNext;
                ei.mEnd.mParent=ei.mItem;
                next.mPrevious=ei.mEnd;
            }

            int cmpEnd=ei.mEnd.compareTo(mEnd);
            if (ei.mEnd.compareTo(mStart)<0)
            {
                mStartPosition++;
                mEndPosition++;
                mInnerAdapter.notifyItemInserted(0);
            }else if (cmpEnd>0)
            {
                mInnerAdapter.notifyItemInserted(mSize);
            }else if (cmpEnd==0)
            {
                //前面的插入到了mEnd后面
                mInnerAdapter.notifyItemInserted(mEndPosition);
            }else
            {
                //end前
                int index=mEndPosition;
                ItemInfo ii=mEnd;
                while(ii!=ei.mEnd)
                {
                    ii=ii.mPrevious;
                    index--;
                }

                mInnerAdapter.notifyItemInserted(index+1);
                mEndPosition++;
            }
            mSize++;
        }
    }

}
