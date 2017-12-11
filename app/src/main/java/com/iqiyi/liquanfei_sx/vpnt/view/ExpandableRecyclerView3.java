package com.iqiyi.liquanfei_sx.vpnt.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.iqiyi.liquanfei_sx.vpnt.tools.Rf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/12/6.
 *
 */

public class ExpandableRecyclerView3 extends RecyclerView implements View.OnClickListener{

    private Adapter mAdapter=null;
    private MAdapter mInnerAdapter=null;
    private boolean mEnableAnimation=true;
    private ExpandableItem mRoot=new ExpandableItem(-1,null);

    private List<int[]> mCachedPosition=new ArrayList<>();

    private Map<View,MAdapter.ListenerInfo> mChildClickListeners=new HashMap<>();

    private ExpandObserver mExpandItemObserver=new ExpandObserver();

    /**
     * TODO when notify
     */
    private AdapterDataObserver mObserver=new AdapterDataObserver() {
        @Override
        public void onChanged() {
            //rangeChanged();
            //mRoot.fresh(mAdapter.getItemCount());
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

        addItemDecoration(new MDecoration());
        super.setAdapter(mInnerAdapter);
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(Adapter adapter) {
        if (mAdapter==null||mAdapter!=adapter) {
            mAdapter = adapter;
            mAdapter.registerAdapterDataObserver(mObserver);
            mAdapter.setObserver(mExpandItemObserver);
        }
        //mAdapter.notifyDataSetChanged();
        mRoot.fresh(mAdapter.getItemCount());
        super.setAdapter(mInnerAdapter);
    }

    @Override
    public void onClick(View v) {
        MAdapter.ListenerInfo li=mChildClickListeners.get(v);
        if (li.mHolder.getAdapterPosition()<0) {
            //holder有可能已经被解除关联
            return;
        }

        int []p=mRoot.get(li.mHolder.getAdapterPosition());
        int expandSize;
        if ((expandSize=mRoot.expand(li.mHolder.getAdapterPosition(),mAdapter.getItemCount(p)))==-1)
        {
            mAdapter.onExpand(p);
            mInnerAdapter.notifyItemRangeInserted(mRoot.getRealPosition(0,p),mAdapter.getItemCount(p));
        }else
        {
            mInnerAdapter.notifyItemRangeRemoved(mRoot.getRealPosition(0,p)+1,expandSize);
        }
        //mInnerAdapter.notifyDataSetChanged();
        OnClickListener l=li.mL;
        if (l!=null&&l!=this)
            l.onClick(v);
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

            int []p=mRoot.get(position);
            mAdapter.onBindViewHolder(holder,p);
            if (mAdapter.canExpand(p))
            {
                View v=holder.itemView;
                OnClickListener l=getOnClickListener(v);
                MAdapter.ListenerInfo li=mChildClickListeners.get(v);
                if (li==null)
                {
                    mChildClickListeners.put(v,new MAdapter.ListenerInfo(l,holder));
                    v.setOnClickListener(ExpandableRecyclerView3.this);
                }else
                {
                    if (l!=ExpandableRecyclerView3.this)
                    {
                        li.mL=l;
                    }
                    li.mHolder=holder;
                }
            }
        }

        @Override
        public int getItemCount() {
            return mRoot.mSize;
        }

        @Override
        public int getItemViewType(int position) {
            if (mAdapter==null)
                return 0;


            return mAdapter.getItemViewType(mRoot.get(position));
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

    public static abstract class Adapter extends RecyclerView.Adapter
    {
        private ExpandObserver mObserver;

        private void setObserver(ExpandObserver o)
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

        public final void notifyFresh(int depth)
        {
            mObserver.onFresh(depth);
        }
    }

    static final int MAXITEM=20;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private class ExpandObserver
    {
        void onAdd(int ...position) {
            int i=0;
            ExpandableItem ei=mRoot;
            for (;i<position.length-1;i++)
            {
                if (ei==null)
                    return;
                ei=ei.findExpand(position[i]);
            }
            if (ei==null)
                return;
            ei.insert(position[i]);

            mInnerAdapter.notifyItemInserted(mRoot.getRealPosition(0,position));
        }

        void onFresh(int depth)
        {
            mRoot.freshExpandChildren(depth);
            mInnerAdapter.notifyDataSetChanged();
        }
    }

    private class MDecoration extends ItemDecoration
    {
        private Drawable mDivider;
        private Paint mPaint;

        private int mOffset=0;

        MDecoration()
        {
            mDivider=getContext().obtainStyledAttributes(new int[]{android.R.attr.listDivider}).getDrawable(0);
            mPaint=new Paint();
            mPaint.setColor(Color.parseColor("#dedede"));
            if (mDivider!=null)
                mOffset=mDivider.getIntrinsicHeight();
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            outRect.set(0, 0, 0, mOffset);
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, State state) {
            super.onDraw(c, parent, state);

            int l = parent.getPaddingLeft();
            int r = parent.getWidth() - parent.getPaddingRight();
            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount-1; i++){
                final View child = parent.getChildAt(i);

                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)child.getLayoutParams();
                final int t = child.getBottom() + params.bottomMargin;
                final int b = t + mDivider.getIntrinsicHeight();
                c.drawRect(l, t, r, b,mPaint);
            }
        }
    }
}
