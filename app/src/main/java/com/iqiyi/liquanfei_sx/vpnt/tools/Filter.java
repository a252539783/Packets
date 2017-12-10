package com.iqiyi.liquanfei_sx.vpnt.tools;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Administrator on 2017/12/4.
 */

public abstract class Filter<T> implements List<T>{

    public static final int NON_EMPTY=0x01;
    public static final int BY_NAME=0x02;
    public static final int BY_PACKAGE=0x04;
    public static final int BY_IP_DEST=0x08;
    public static final int BY_PORT_DEST=0x10;
    public static final int BY_PORT_SOURCE=0x20;

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
        }else
        {
            mFiltered[mCurrentKey].clear();
        }
        mCachedIndex[mCurrentKey]=0;

        if (reload)
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
                //add(mSrc.get(mCachedIndex[mCurrentKey]));
                addFromInner();
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
//                if (add(mSrc.get(mCachedIndex[mCurrentKey]))) {
                if (addFromInner()) {
                    LoadListener ll=l.get();
                    if (ll!=null)
                    {
                        ll.onLoadOne(size()-1);
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

    @Override
    public int size()
    {
        return size(mCurrentKey);
    }

    @Override
    public boolean isEmpty() {
        return size()==0;
    }

    @Override
    public boolean contains(Object o) {
        return mFiltered[mCurrentKey].contains(o);
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return mFiltered[mCurrentKey].iterator();
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(@NonNull T1[] a) {
        return mFiltered[mCurrentKey].toArray(a);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return mFiltered[mCurrentKey].toArray();
    }

    @Override
    public boolean remove(Object o) {
        boolean res=false;

        for (int i=0;i<mFiltered.length;i++)
        {
            if (i==mCurrentKey)
            {
                if (res=mFiltered[mCurrentKey].remove(o))
                {
                    mCachedIndex[mCurrentKey]--;
                }
            }else
            {
                if (mFiltered[i].remove(o))
                {
                    mCachedIndex[i]--;
                }
            }
        }

        return res;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {
        for (int i=0;i<mFiltered.length;i++)
        {
            mFiltered[i].clear();
            mCachedIndex[i]=0;
        }
    }

    @Override
    public T set(int index, T element) {
        return null;
    }

    @Override
    public T remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<T> listIterator() {
        return mFiltered[mCurrentKey].listIterator();
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return mFiltered[mCurrentKey].listIterator(index);
    }

    @NonNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return mFiltered[mCurrentKey].subList(fromIndex, toIndex);
    }

    public int size(int key)
    {
        return mFiltered[key].size();
    }

    @Override
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

    boolean addFromInner()
    {
        if (mCurrentKey==0)
            return false;

        if (mSrc.size()==mCachedIndex[mCurrentKey])
            return false;

        T o=mSrc.get(mCachedIndex[mCurrentKey]);
        boolean accept=filter(mCurrentKey,o);

        if (accept)
        {
            mFiltered[mCurrentKey].add(o);
        }
        mCachedIndex[mCurrentKey]++;

        return accept;
    }

    @Override
    public void add(int index, T element) {
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

    public interface LoadListener
    {
        void onLoadOne(int index);
        void onLoadComplete();
    }
}
