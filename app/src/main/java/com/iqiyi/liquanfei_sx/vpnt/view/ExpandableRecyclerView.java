package com.iqiyi.liquanfei_sx.vpnt.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.iqiyi.liquanfei_sx.vpnt.tools.Rf;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Administrator on 2017/9/28.
 * TODO
 *
 * RecyclerView嵌套之后，如果不处理好嵌套滚动那么将会导致
 * 内部重用机制失效（取消嵌套滚动时必须让所有的item为自适应大小
 * 才能正常显示，此时会一次性绘制所有元素，无法重用）
 * 1：使用嵌套滚动
 * 2：伪嵌套
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
        setLayoutManager(new LinearLayoutManager(getContext()));
        getLayoutManager().setAutoMeasureEnabled(true);
        ((SimpleItemAnimator)getItemAnimator()).setSupportsChangeAnimations(false);
        getItemAnimator().setRemoveDuration(0);
        getItemAnimator().setMoveDuration(0);
        getItemAnimator().setChangeDuration(0);
        getItemAnimator().setAddDuration(0);
        setNestedScrollingEnabled(false);
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

    public void setExpandable(boolean can)
    {
        mCanExpand =can;
    }

    public Adapter getAdapter()
    {
        return mAdapter;
    }

    public void setAdapter(Adapter adapter) {
        if (mAdapter==null||mAdapter!=adapter) {
            mAdapter = adapter;
            mAdapter.registerAdapterDataObserver(mObserver);
        }
        //mAdapter.notifyDataSetChanged();
        mIsExpandView.clear();
        for (int i=0;i<mAdapter.getItemCount();i++)
        {
            mIsExpandView.add(false);
        }
        super.setAdapter(mInnerAdapter);
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
        if (isExpand!=null&& ((Boolean) isExpand))
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
        expandItem(li.mMaskPosition);

        OnClickListener l=li.mL;
        if (l!=null&&l!=this)
            l.onClick(v);
    }

    public static abstract class Adapter<VH extends ViewHolder> extends RecyclerView.Adapter<VH>
    {
        public abstract void onBindExpandView(ExpandableRecyclerView view, int position);

        public abstract boolean canExpand(int position);
    }

     private class MAdapter extends RecyclerView.Adapter
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
                        mChildClickListeners.put(v,new ListenerInfo(l,position,maskPosition));
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
}
