package com.iqiyi.liquanfei_sx.vpnt.tools;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/4.
 */

public abstract class Filter<T> {

    private List<T>[] mFiltered;
    private List<T> mSrc;

    private int[] mCachedIndex;

    private boolean mLoading=false;
    private boolean mLoadStopped=true;

    private int mCurrentKey =0;

    public Filter(int size,List<T> src)
    {
        mSrc=src;
        mFiltered=new List[size+1];
        mFiltered[0]=src;
        mCachedIndex=new int[size+1];
    }

    /**
     * 设置过滤器关键字的同时会遍历加载整个源列表
     */
    public void setKey(int key, WeakReference<LoadListener> l,boolean reload)
    {
        if (key<=0||key>=mFiltered.length)
            key=0;

        mCurrentKey =key;
        if (mFiltered[mCurrentKey]==null)
        {
            mFiltered[mCurrentKey]=new ArrayList<>();
        }

        reload(l);

    }

    public void reload(WeakReference<LoadListener> l)
    {

        if (mCurrentKey!=0)
        {
            mCachedIndex[mCurrentKey]=0;
            load(l);
        }
    }

    public List<T> getSrc()
    {
        return mSrc;
    }

    public void load(WeakReference<LoadListener> l)
    {
        if (l==null)
        {
            if (mCurrentKey==0)
                return;

            while(!mLoadStopped);   //等待上一个加载结束

            mLoading=true;
            mLoadStopped=false;
            while(mLoading&&mCachedIndex[mCurrentKey]!=mSrc.size())
            {
                add(mSrc.get(mCachedIndex[mCurrentKey]));
            }
            mLoadStopped=true;
        }else
        {
            if (mCurrentKey==0) {
                LoadListener ll=l.get();
                if (ll!=null)
                    ll.onLoadComplete();
                return;
            }

            while(!mLoadStopped);   //等待上一个加载结束

            mLoading=true;
            mLoadStopped=false;
            while(mLoading&&mCachedIndex[mCurrentKey]!=mSrc.size())
            {
                if (add(mSrc.get(mCachedIndex[mCurrentKey]))) {
                    LoadListener ll=l.get();
                    if (ll!=null)
                    {
                        ll.onLoadOne(mSrc.size()-1);
                    }else
                    {
                        return;
                    }
                }
            }
            mLoadStopped=true;

            LoadListener ll=l.get();
            if (ll!=null)
            {
                ll.onLoadComplete();
            }
        }
    }

    public void stopLoad()
    {
        mLoading=false;
    }

    public int size()
    {
        return size(mCurrentKey);
    }

    public int size(int key)
    {
        return mFiltered[key].size();
    }

    public boolean add(T o)
    {
        mFiltered[0].add(o);

        if (mCurrentKey==0)
            return true;

        boolean accept=filter(mCurrentKey,o);

        if (accept)
        {
            mFiltered[mCurrentKey].add(o);
        }
        mCachedIndex[mCurrentKey]++;

        return accept;
    }

    public T get(int index)
    {
        return get(mCurrentKey,index);
    }

    public T get(int key,int index)
    {
        if (key<=0||key>=mFiltered.length)
        {
            key=0;
        }

        return mFiltered[key].get(index);
    }

    public abstract boolean filter(int key,T o);

    interface LoadListener
    {
        void onLoadOne(int index);
        void onLoadComplete();
    }
}
