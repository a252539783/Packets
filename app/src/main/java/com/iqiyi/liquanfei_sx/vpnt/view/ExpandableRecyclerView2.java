package com.iqiyi.liquanfei_sx.vpnt.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.iqiyi.liquanfei_sx.vpnt.history.HistoryAdapter2;
import com.iqiyi.liquanfei_sx.vpnt.tools.Rf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * in fact,this only produce one RecyclerView's instance.
 */

public class ExpandableRecyclerView2 extends RecyclerView implements View.OnClickListener{

    private boolean mUsingDataSetChanged=true;

    private Adapter mAdapter=null;

    private MAdapter mInnerAdapter=null;

    private Map<View,MAdapter.ListenerInfo> mChildClickListeners=new HashMap<>();

    private Map<ItemInfo,Integer> mRealPosition=new HashMap<>();

    private ExpandInfo mExpand=new ExpandInfo(null);

    private ExpandItemAddObserver mExpandObserver=new ExpandItemAddObserver();

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

    private boolean mCanMultiExpandable=true;

    private boolean mEnableAnimation=true;

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

    boolean expandItem(ItemInfo ii,int position)
    {
        if (position>=mSize) {
            Log.e("ExpandableRecyclerView","Exception: expand a item out of range");
            return false;
        }

        ExpandInfo ei=mExpand.get(ii);
        if (ei==null)
        {
            //Log.e("xx","expand "+position+":startP "+mStartPosition+":endP:"+mEndPosition);
            expandItemUnchecked(ii,position);
            return true;
        }else
        {
            collapseItemUnchecked(ei,position);
            return false;
        }
    }

    public boolean isExpand(int []position)
    {
        return getExpandInfo(position)!=null;
    }

    private int getRealPosition(int position)
    {
        int res=0;
        int resi=0;
        int fresi=0;
        int p=position;
        ItemInfo ii=mRoot;
        ExpandInfo ei=mExpand;
        while (ei!=null)
        {
            if (ei.mItem!=null)
            {
                if (ei.mEnd!=null)
                {
                    p=ei.mEnd.mIndex+1;
                }else
                {
                    p=0;
                }
            }

            boolean find=false;
            for (int i=0;i<p;i++)
            {
                Integer o=mRealPosition.get(ii);
                if (o!=null)
                {
                    res=o;
                    resi=i;
                    find=true;
                }

                ii=ii.mDNext;
            }

            if (!find)
            {
                break;
            }
            //进入最后一个被展开item的下一层
            ei=ei.get(resi);

            if (p==position)
            {
                fresi=resi;
            }
        }

        return (position-fresi)+res;
    }

    private ExpandInfo getExpandInfo(int []position)
    {
        ExpandInfo ei=mExpand;
        for (int aPosition : position) {
            ei = ei.get(aPosition);

            if (ei == null)
                return null;
        }

        return ei;
    }

    private boolean expandItem(View v)
    {
        MAdapter.ListenerInfo info=mChildClickListeners.get(v);
        if (info==null)
            return false;

        return expandItem(info.mii,info.mHolder.getAdapterPosition());
    }

    private boolean collapseItem(int [] position)
    {
        ExpandInfo ei=getExpandInfo(position);

        if (ei!=null)
        {
            collapseItemUnchecked(ei,mRealPosition.get(ei.mItem));
            return true;
        }
        return false;
    }

    private void collapseItemUnchecked(ExpandInfo ei,int position)
    {
        mExpand.remove(ei.mItem);
        if (ei.mEnd==null)
            return;

        int size=ei.size();

        ItemInfo next=ei.next();
        if (next!=null)
            next.mPrevious=ei.mItem;
        ei.mItem.mNext=next;

        mSize-=size;

        //mEndPosition-=size;
        //mEndPosition=mStartPosition;
        //mEnd=mStart;

        //here can't ensure the position of mEnd,so make it equal to mStart simply.
        mEnd=mStart=ei.mItem;
        mEndPosition=mStartPosition=position;

        if (mUsingDataSetChanged) {
            mInnerAdapter.notifyDataSetChanged();
        }
        else {
            mInnerAdapter.notifyItemRangeRemoved(position + 1, size);
        }

    }

    private void expandItemUnchecked(ItemInfo ii,int position)
    {
        ExpandInfo ei=mExpand.put(ii);

        int []index=contentPosition(ii,position);

        int size=mAdapter.getItemCount(index);
        freshPosition(position,size);
        mRealPosition.put(ii,position+size);

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
                if (ii.mDepth==ii.mNext.mDepth)
                {
                    ii.mNext.mDPrevious=ii;
                    ii.mDNext=ii.mNext;
                }
                ii=ii.mNext;
            }
            ii.mNext=last;
            if (last!=null) {
                last.mPrevious = ii;
            }
            mEnd=mStart=ei.mItem;
            mEndPosition=mStartPosition=position;

            if (mUsingDataSetChanged) {
                mInnerAdapter.notifyDataSetChanged();
            }
            else {
                mInnerAdapter.notifyItemRangeInserted(position + 1, size);
            }

            mSize+=size;
            ei.mEnd=ii;
        }


        mAdapter.onExpand(index);
    }

    private void freshPosition(int position,int size)
    {
        for (Map.Entry<ItemInfo, Integer> entry : mRealPosition.entrySet()) {
            if (entry.getValue() > position) {
                entry.setValue(entry.getValue() + size);
            }
        }
    }

    private void freshPosition(int[] position,int size)
    {
        for (Map.Entry<ItemInfo, Integer> entry : mRealPosition.entrySet()) {
            ItemInfo ii=entry.getKey();
            for (int i=0;i<position.length;i++)
            {
                if (ii.mIndex>=position[i])
                {
                    entry.setValue(entry.getValue() + size);
                    break;
                }
            }
        }
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


    //not used...
    private int []contentPosition(int position)
    {
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
            ii=mStart;
            for(int index=mStartPosition;index!=position;index++)
            {
                ii=ii.mNext;
            }
        }

        return contentPosition(ii,position);
    }

    SparseArray<Position> mCachedPosition=null;

    /**
     * get the item's position
     * @param item target item
     * @param position the real position
     * @return a array which shows the item's position by put all
     * parent's position and the item's position
     */
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
            if (item==null) {
                Log.e("ExpandableRecyclerView","exception:contentPosition error position");
                break;
            }
            rposition[i]=item.mIndex;
            item=item.mParent;
        }

        return rposition;
    }

    //not cached
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
            //simply new
            mRoot=new ItemInfo(0,0);
            mStart=mEnd=mRoot;
            mExpand.mEnd=mRoot;
            return mRoot;
        }

        if (mStartPosition==position)
        {
            return mStart;
        }else
        if (mStartPosition>position)
        {
            /*
             * before mStart
             */
            if (mStart.mPrevious==null)
            {
                /*
                 * finding a not loaded item here means a bug has been produced somewhere.
                 */
                Log.e("ExpandableRecyclerView","Exception:load previous null  position:"+position+" start:"+mStartPosition);
            }else
            {
                while(mStartPosition!=position) {
                    mStart = mStart.mPrevious;
                    mStartPosition--;
                }
                return mStart;
            }
        }else if (mEndPosition>position)
        {
            /*
              between mStart and mEnd,we make mEnd go back.
             */
            while(mEndPosition!=position)
            {
                mEnd=mEnd.mPrevious;
                mEndPosition--;
            }

            return mEnd;
        }else if (mEndPosition<position)
        {
            while(mEndPosition!=position)
            {
                if (mEnd.mNext==null)
                {
                    int[] index=contentPosition(mEnd,mEndPosition);
                    ItemInfo parent=mEnd.mParent;
                    ItemInfo preParent=mEnd;
                    for (int i=1;i<=index.length;i++)
                    {
                        //find the next item
                        if (index[index.length-i]!=mAdapter.getItemCount(index,index.length-i)-1)
                        {
                            mEnd.mNext=new ItemInfo(i-1,index[index.length-i]+1);
                            mEnd.mNext.mPrevious=mEnd;
                            mEnd.mNext.mParent=parent;
                            mEnd.mNext.mDPrevious=preParent;
                            if (preParent!=null)
                            preParent.mDNext=mEnd.mNext;

                            if (mEnd.mNext.mDepth==0)
                            {
                                mExpand.mEnd=mEnd.mNext;
                            }
                            break;
                        }
                        preParent=parent;
                        if (parent!=null)
                            parent=parent.mParent;
                    }
                }

                mEnd=mEnd.mNext;
                mEndPosition++;
            }
            /*
             * make the distance between mStart and mEnd be a appropriate number
             */
            if (getChildCount()!=0){
                while (mEndPosition-mStartPosition>getChildCount()+5)
                {
                    mStart=mStart.mNext;
                    mStartPosition++;
                }

                while (mEndPosition-mStartPosition<(getChildCount()>mAdapter.getItemCount()?mAdapter.getItemCount()-1:getChildCount()-1))
                {
                    if (mStart.mPrevious!=null)
                    {
                        mStart=mStart.mPrevious;
                        mStartPosition--;
                    }

                    if (mEnd.mNext!=null)
                    {
                        mEnd=mEnd.mNext;
                        mEndPosition++;
                    }
                }
            }
        }else if (mEndPosition==position)
        {
        }

        if (mEnd==null)
        {
            if (mStart==null)
            {
                mStart=mRoot;
                mStartPosition=0;
            }
            mEnd=mStart;
            mEndPosition=mStartPosition;
            Log.e("xx","preload error"+position);
            return preLoad(position);
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
     * TODO more callback
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

            //Log.e("xx","bind "+position+":startP "+mStartPosition+":endP:"+mEndPosition);
            try {
                mAdapter.onBindViewHolder(holder, maskPosition);
                if (holder instanceof HistoryAdapter2.H2)
                {
                }
            }catch (Exception e)
            {
                Log.e("xx",e.toString());
            }
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
            /**
             * for listening the click action we must save the OnClickListener
             * that user sets
             */
            OnClickListener mL;

            ItemInfo mii;

            //this ViewHolder is for purpose of getting item's real position.
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
        ItemInfo mDNext=null,mDPrevious=null;
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
         * here mPosition puts the every item parent's position and this item's position.
         */
        int []mPosition;

        //the real position in recyclerView,but currently isn't used.
        int mIntPosition;

        Position(int p,int[] po)
        {
            mPosition=po;
            mIntPosition=p;
        }
    }

    /**
     * a tree show the hierarchy for our ExpandableRecyclerView
     */
    private static class ExpandInfo
    {
        //we make the child's position be the key
        SparseArray<ExpandInfo> mChildren=null;

        /*
        Every ExpandInfo is combined with am ItemInfo and this ItemInfo is mItem.
        mEnd is the last child for mItem.
         */
        ItemInfo mItem,mEnd=null;

        ExpandInfo(ItemInfo item)
        {
            mItem=item;
        }

        /**
         * get a child for the given child's position
         * @param position the direct child's position
         * @return child
         */
        ExpandInfo get(int position)
        {
            return mChildren==null?null:mChildren.get(position);
        }

        /**
         * get the corresponding ExpandInfo for the given ItemInfo.
         * @param ii the ItemInfo
         * @return the corresponding ExpandInfo
         */
        ExpandInfo get(ItemInfo ii)
        {
            if (ii==null)
                return null;

            //for direct child,simply find and return it
            if (ii.mParent==mItem)
                return mChildren==null?null:mChildren.get(ii.mIndex);

            //otherwise find the corresponding ExpandInfo for ii's parent and call its get.
            ExpandInfo ei=get(ii.mParent);
            return ei==null?null:ei.get(ii);
        }

        //calculate the next item which isn't mItem's child.
        ItemInfo next()
        {
            if (mEnd==null)
            {
                return mItem.mNext;
            }

            //if the last of the children was expanded,call it
            if (mChildren!=null&&mChildren.size()!=0) {
                ExpandInfo ei=mChildren.valueAt(mChildren.size() - 1);

                if (ei.mItem==mEnd)
                    return ei.next();
            }

            //there is no expanded child
            return mEnd.mNext;
        }

        //remove
        void remove(ItemInfo ii)
        {
            if (ii==null)
                return;

            if (ii.mParent==mItem&&mChildren!=null) {
                mChildren.remove(ii.mIndex);
            }else
            {
                get(ii.mParent).remove(ii);
            }
        }

        //calculate the how many expanded items there are
        int size()
        {
            if (mChildren==null||mChildren.size()==0)
                return mEnd==null?0:(mEnd.mIndex+1);

            int size=mEnd==null?0:(mEnd.mIndex+1);
            for (int i=0;i<mChildren.size();i++)
            {
                size+=mChildren.valueAt(i).size();
            }

            return size;
        }

        /**
         * put an ItemInfo into a correct position.
         * @param ii the ItemInfo we want add
         * @return the corresponding ExpandInfo for the given ItemInfo.
         */
        ExpandInfo put(ItemInfo ii)
        {
            if (ii==null)
                return null;

            if (mChildren==null)
                mChildren=new SparseArray<>();

            ExpandInfo ei;

            /*
            for direct child
             */
            if (ii.mParent==mItem)
            {
                //for a existed child
                if ((ei=mChildren.get(ii.mIndex))!=null) {
                    return ei;
                }

                mChildren.put(ii.mIndex,ei=new ExpandInfo(ii));
                return ei;
            }

            /*
            isn't a direct child,solving the child's parent first and we can add ii.
             */
            if (ii.mParent!=null)
            {
                //recursion
                ei=put(ii.mParent);
            }else
            {
                /*
                don't put an ItemInfo which isn't our child
                 */
                return null;
            }

            /*
            ei now is the corresponding ExpandInfo for ii's parent,
            put ite directly.
             */
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

                //unexpanded item,ignore it
                if(ei==null)
                    return;
            }

            mSize++;
            if (position.length==0)
            {
                freshPosition(position,1);
            }else
            {
                freshPosition(position,1);
                //freshPosition(mRealPosition.get(ei.mItem)-1,1);
            }

            int realPosition=-1;
            if (position.length!=1) {
                realPosition = mRealPosition.get(ei.mItem);
            }
            else
            {
                realPosition=getRealPosition(position[0]);
            }

            if (ei.mEnd==null)
            {
                if (ei.mItem!=null)
                {
                    ei.mEnd=new ItemInfo(ei.mItem.mDepth+1,0);
                    ei.mEnd.mNext=ei.mItem.mNext;
                    ei.mItem.mNext=ei.mEnd;
                }else
                {
                    ei.mEnd=new ItemInfo(0,0);
                    if (mRoot==null)
                    {
                        mRoot=mStart=mEnd=ei.mEnd;
                    }
                }
                ei.mEnd.mPrevious=ei.mItem;
                ei.mEnd.mParent=ei.mItem;

                if (ei.mEnd.mNext!=null) {
                    ei.mEnd.mNext.mPrevious = ei.mEnd;
                }
            }else
            if (ei.mEnd.mIndex>=position[i])
            {//TODO 完成中间添加
                if (position[i]>ei.mEnd.mIndex/2)
                {

                }else
                {
                    ItemInfo ii=ei.mItem;
                    if (ii==null) {
                        ii = mRoot;
                        realPosition=position[i];
                        if (position[i]==0)
                        {
                            ii.mPrevious=new ItemInfo(0,0);
                            ii.mPrevious.mNext=ii;
                            ii.mPrevious.mDNext=ii;
                            ii.mDPrevious=ii.mPrevious;

                            while(ii!=null)
                            {
                                ii.mIndex++;
                                ii=ii.mDNext;
                            }
                        }else
                        {
                            //TODO
//                            for (int j=1;j!=position[i];j++)
//                            {
//                                ii=ii.mDNext;
//                            }
//                            ItemInfo iii=ii.mDNext;
//                            iii.mDPrevious=new ItemInfo(0,position[i]);
//                            iii.mDPrevious.mDPrevious=ii;
//                            iii.mDPrevious.mNext=iii;

                        }
                    }
                }
            }else
            {
                //after ei.mEnd
                ItemInfo next=ei.mEnd.mNext;
                ei.mEnd.mNext=new ItemInfo(ei.mEnd.mDepth,ei.mEnd.mIndex+1);
                ei.mEnd.mNext.mNext=next;
                ei.mEnd.mNext.mPrevious=ei.mEnd;
                ei.mEnd=ei.mEnd.mNext;
                ei.mEnd.mParent=ei.mItem;

                if (next!=null)
                    next.mPrevious=ei.mEnd;
            }

            int cmpEnd=realPosition-mEndPosition;//ei.mEnd.compareTo(mEnd);

            //if (ei.mEnd.compareTo(mStart)<0)
            if (realPosition<=mStartPosition)
            {
                //before mStart
                mStartPosition++;
                mEndPosition++;
            }
            else if (cmpEnd>0)
            {
                //after mEnd
            }else if (cmpEnd<=0)
            {
                //before mEnd
                mEndPosition++;
            }

            if (mUsingDataSetChanged) {
                mInnerAdapter.notifyDataSetChanged();
            }
            else {
                mInnerAdapter.notifyItemInserted(realPosition);
            }
        }
    }

}
