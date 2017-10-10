package com.iqiyi.liquanfei_sx.vpnt;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import com.iqiyi.liquanfei_sx.vpnt.tools.Rf;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Administrator on 2017/9/28.
 */

public class ExpandableRecyclerView extends RecyclerView implements View.OnClickListener {

    private static int TYPE_RECYCLER=-1000;

    private Adapter mAdapter=null;
    private MAdapter mInnerAdapter=null;
    private ArrayMap<View,MAdapter.ListenerInfo> mChildClickListeners=new ArrayMap<>();

    private AdapterDataObserver mObserver=new AdapterDataObserver() {
        @Override
        public void onChanged() {
            rangeChanged();
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
            itemInserted(positionStart,itemCount);
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

    private boolean mCanMultiExpandable=false;

    private boolean mCanExpand =false;
    private boolean mIsExpand=false;

    private List<Boolean> mIsExpandView=new LinkedList<>();

    public ExpandableRecyclerView(Context context) {
        super(context);
        init();
    }

    public ExpandableRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExpandableRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
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
        setLayoutManager(new MLinearLayoutManager(getContext()));
        getLayoutManager().setAutoMeasureEnabled(true);
        ((SimpleItemAnimator)getItemAnimator()).setSupportsChangeAnimations(false);
        getItemAnimator().setRemoveDuration(0);
        getItemAnimator().setMoveDuration(0);
        getItemAnimator().setChangeDuration(0);
        getItemAnimator().setAddDuration(0);
        setNestedScrollingEnabled(false);
        mInnerAdapter=new MAdapter();
        super.setAdapter(mInnerAdapter);
    }

    public void setExpandable(boolean can)
    {
        mCanExpand =can;
    }

    public Adapter getAdapter()
    {
        return mAdapter;
    }

    public void setAdapter(Adapter adapter) {
        mAdapter=adapter;
        mAdapter.registerAdapterDataObserver(mObserver);
        mIsExpandView.clear();
        for (int i=0;i<mAdapter.getItemCount();i++)
        {
            mIsExpandView.add(false);
        }
    }

    MAdapter innerAdapter()
    {
        return mInnerAdapter;
    }

    public void expandItem(int position)
    {
        if (position>=mAdapter.getItemCount())
            return ;

        int real=getRealPosition(position);

        if (real+1==mIsExpandView.size())
        {
            expandItemUncheck(real+1);
            return ;
        }
        Object isExpand=mIsExpandView.get(real+1);
        if (isExpand!=null&&((Boolean)isExpand)==true)
        {
            mIsExpandView.remove(real+1);
            mInnerAdapter.notifyItemRemoved(real+1);
            return ;
        }
        expandItemUncheck(real+1);
    }

    private void expandItemUncheck(int position)
    {

        mIsExpandView.add(position,true);
        mInnerAdapter.notifyItemInserted(position);
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
        ListIterator<Boolean> it=mIsExpandView.listIterator();
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

    private int getRealPosition(int position)
    {
        int realPosition=0;
        Iterator<Boolean> it=mIsExpandView.iterator();
        it.next();
        position--;
        while (position!=-1)
        {
            if (!it.next())
            {
                position--;
            }else
            {
            }
            realPosition++;
        }
        return realPosition;
    }

    private int contentPosition(int position)
    {
        int maskPosition=0;
        Iterator<Boolean> it=mIsExpandView.iterator();
        it.next();
        position--;
        while (position!=-1)
        {
            if (!it.next())
            {
                maskPosition++;
            }else
            {
            }
            position--;
        }

        return maskPosition;
    }

    @Override
    public void onClick(View v) {
        MAdapter.ListenerInfo li=mChildClickListeners.get(v);
        expandItem(li.mPosition);

        OnClickListener l=li.mL;
        if (l!=null&&l!=this)
            l.onClick(v);
    }

    public static abstract class Adapter<VH extends ViewHolder> extends RecyclerView.Adapter<VH>
    {
        public abstract void onBindExpandView(ExpandableRecyclerView view, int position);

        public abstract boolean canExpand(int position);
    }

     class MAdapter extends RecyclerView.Adapter
    {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType!=TYPE_RECYCLER)
                return mAdapter.onCreateViewHolder(ExpandableRecyclerView.this,viewType);

            ExpandableRecyclerView rv=new ExpandableRecyclerView(getContext());
            return new ViewHolder(rv) {};
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            int maskPosition=contentPosition(position);
            if (mIsExpandView.get(position))
            {
                ExpandableRecyclerView v=(ExpandableRecyclerView)holder.itemView;
                //v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,400));
                mAdapter.onBindExpandView(v,maskPosition);
                v.setExpandable(true);
            }
            else {
                mAdapter.onBindViewHolder(holder, maskPosition);
                if (mAdapter.canExpand(maskPosition))
                {
                    View v=holder.itemView;
                    OnClickListener l=getOnClickListener(v);
                    ListenerInfo li=mChildClickListeners.get(v);
                    if (li==null)
                    {
                        mChildClickListeners.put(v,new ListenerInfo(l,maskPosition,position));
                        v.setOnClickListener(ExpandableRecyclerView.this);
                    }else
                    {
                        if (l!=ExpandableRecyclerView.this)
                        {
                            li.mL=l;
                        }
                        li.mPosition=position;
                        li.mMaskPosition=maskPosition;
                    }
                }
            }
        }

        @Override
        public int getItemCount() {

            return mIsExpandView.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (mIsExpandView.get(position))
                return TYPE_RECYCLER;
            int maskPosition=contentPosition(position);

            return mAdapter.getItemViewType(maskPosition);
        }

        class ListenerInfo
        {
            OnClickListener mL;
            int mPosition;
            int mMaskPosition;

            ListenerInfo(OnClickListener l,int position,int maskPosition)
            {
                this.mL =l;
                this.mPosition =position;
                this.mMaskPosition =maskPosition;
            }
        }
    }

    private class MLinearLayoutManager extends LinearLayoutManager
    {
//
//        @Override
//        public void onMeasure(Recycler recycler, State state, int widthSpec,int heightSpec) {
//            int measuredHeight=MeasureSpec.getSize(heightSpec);
//            int measuredWidth = MeasureSpec.getSize(widthSpec);
//            if (!mCanExpand)
//            {
//                //super.onMeasure(recycler, state, widthSpec, heightSpec);
//                //return ;
//            }
//
//            int height=0;
//            for (int i=0;i<mInnerAdapter.getItemCount();i++)
//            {
//                try {
//                    View view = recycler.getViewForPosition(0);
//                    if (view != null) {
//                        measureChild(view, widthSpec, heightSpec);
//                        height += view.getMeasuredHeight();
//                    } else {
//                        break;
//                    }
//                }catch (IndexOutOfBoundsException e)
//                {
//                    break;
//                }
//            }
//            if (height>measuredHeight)
//            {
//                height=measuredHeight;
//            }
//            setMeasuredDimension(measuredWidth, height);
//        }

        public MLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public void onMeasure(Recycler recycler, State state, int widthSpec, int heightSpec) {
            super.onMeasure(recycler, state, widthSpec, heightSpec);
        }
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
        //return true;
    }
}
