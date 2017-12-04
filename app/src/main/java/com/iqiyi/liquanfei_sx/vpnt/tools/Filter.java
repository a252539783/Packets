package com.iqiyi.liquanfei_sx.vpnt.tools;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/4.
 */

public abstract class Filter {

    private List[] mFiltered;
    private List mSrc;

    private int[] mCachedIndex;

    private boolean mLoading=false;
    private boolean mLoadStopped=true;

    private int mCurrentKey =0;

    public Filter(int size,List src)
    {
        mSrc=src;
        mFiltered=new List[size];
        mCachedIndex=new int[size];
    }

    /**
     * 设置过滤器关键字的同时会遍历加载整个源列表
     */
    public void setKey(int key, WeakReference<LoadListener> l,boolean reload)
    {
        mCurrentKey =key;
        if (reload)
        {
            reload(key,l);
        }else {
            load(key, l);
        }
    }

    public void reload(WeakReference<LoadListener> l)
    {
        reload(mCurrentKey,l);
    }

    public void reload(int key, WeakReference<LoadListener> l)
    {
        mCachedIndex[key]=0;
        load(key,l);
    }

    public void load(int key, WeakReference<LoadListener> l)
    {
        while(!mLoadStopped);   //等待上一个加载结束

        mLoading=true;
        mLoadStopped=false;
        while(mLoading&&mCachedIndex[key]!=mSrc.size())
        {
            if (add(mSrc.get(mCachedIndex[key]))) {
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

    public boolean add(Object o)
    {
        boolean accept=filter(mCurrentKey,o);

        if (accept)
        {
            mFiltered[mCurrentKey].add(o);
        }
        mCachedIndex[mCurrentKey]++;

        return accept;
    }

    public Object get(int index)
    {
        return get(mCurrentKey,index);
    }

    public Object get(int key,int index)
    {
        if (key<0||key>=mFiltered.length)
        {
            return null;
        }

//        List list=mFiltered[key];
//        if (list==null)
//        {
//            list=mFiltered[key]=new ArrayList();
//        }
//        while(list.size()<=index&&mCachedIndex[key]<mSrc.size())
//        {
//            Object o=mSrc.get(mCachedIndex[key]);
//            if (filter(key,o))
//            {
//                list.add(o);
//            }
//
//            mCachedIndex[key]++;
//        }

        return mFiltered[key].get(index);
    }

    abstract boolean filter(int key,Object o);

    interface LoadListener
    {
        void onLoadOne(int index);
        void onLoadComplete();
    }
}
