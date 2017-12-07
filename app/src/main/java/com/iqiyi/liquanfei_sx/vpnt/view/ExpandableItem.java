package com.iqiyi.liquanfei_sx.vpnt.view;

import android.util.SparseArray;

import com.iqiyi.liquanfei_sx.vpnt.tools.LinkedNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/12/7.
 */

public class ExpandableItem {

    private static List<int[]> mCachedPosition=new ArrayList<>();
    static
    {
        for (int i=0;i<5;i++)
        {
            mCachedPosition.add(new int[i+1]);
        }
    }

    static final int MAXITEM=20;

    int mIndex=0;
    private int mSize=0;
    private int mDepth;
    private int mChildCount=0;
    ExpandableItem mParent;
    int mStartPosition=0,mEndPosition=0,mChildPosition=0;
    LinkedNode<ExpandableItem> mChild,mStart,mEnd;
    private HashMap<Integer,LinkedNode<ExpandableItem>> mExpands=new HashMap<>();

    ExpandableItem(int depth,ExpandableItem parent)
    {
        mParent=parent;
        mDepth=depth;
    }

    void sizeChange(int size)
    {
        mSize+=size;
        if (mParent!=null)
        {
            mParent.sizeChange(size);
        }
    }

    void expand(int position,int initSize)
    {
        if (position==-1)
        {
            int d=initSize-mSize;
            fresh(initSize);
            mParent.sizeChange(d);
            mParent.addExpand(mIndex,this);
            return;
        }

        find(position).expand(-1,initSize);
    }

    void addExpand(int index,ExpandableItem ei)
    {
        mExpands.put(index,new LinkedNode<ExpandableItem>(ei));
    }

    void fresh(int size)
    {
        mExpands.clear();
        if (mEnd!=null&&mEnd.o.mIndex>=size)
        {
            int d=mEnd.o.mIndex-size+1;
            LinkedNode<ExpandableItem> ei=mStart;
            ei.o.mIndex-=d;
            ei=ei.next;
            while(ei!=mStart)
            {
                ei.o.mIndex-=d;
                ei=ei.next;
            }
            mChildPosition-=d;
        }

        mSize=size;
    }

    int[] get(int position)
    {
        if (true)
        {
            return mCachedPosition.get(find(position).mDepth);
        }

        if (position>=mSize||position<0)
            return null;

        if (mChild==null)
        {
            mEnd=mStart=mChild=new LinkedNode<>(new ExpandableItem(mDepth+1,this));
        }

        if (mChildPosition==position)
        {
            //当前child就是目标
            mEnd=mChild;
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
                        mEnd=mChild;
                        return mChild.o.get(position);
                    }
                }

                mChildPosition+=1+mChild.o.mSize;

                if (mChild.next==null)
                {
                    //添加下一个
                    if (mChildCount==MAXITEM)
                    {
                        //如果个数满了，把第一个链接到最后一个成环
                        mChild.linkThisBefore(mStart);
                    }else
                    {
                        //new
                        mChildCount++;
                        mEnd=mChild.linkThisBefore(new ExpandableItem(mDepth+1,this));
                    }
                }
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
                if (mChild==mStart)
                {
                    mStart=mStart.next;
                    mEnd=mChild;
                }
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
                    if (mChildCount==MAXITEM)
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
                    }
                    {
                        //上一个不是被展开的
                        //如果旧的被展开则插入新的
                        if (mChild.next.o.mSize!=0)
                        {
                            mChild.replaceThisPrevious(new LinkedNode<>(new ExpandableItem(mDepth+1,this)));
                        }else
                        {
                            //否则更新
                            mChild.previous.o.mIndex=mChild.o.mIndex-1;
                        }
                    }

                    mChildPosition-=mChild.previous.o.mSize+1;

                    mChild=mChild.previous;
                    if (mChild==mEnd)
                    {
                        mEnd=mChild.previous;
                        mStart=mChild;
                    }

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
            //循环退出后说明当前child已经是目标
            return saveChildPosition();
        }
    }

    ExpandableItem find(int position)
    {
        if (position>=mSize||position<0)
            return null;

        if (mChild==null)
        {
            mEnd=mStart=mChild=new LinkedNode<>(new ExpandableItem(mDepth+1,this));
        }

        if (mChildPosition==position)
        {
            //当前child就是目标
            mEnd=mChild;
            saveChildPosition();
            return mChild.o;
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
                        mEnd=mChild;
                        return mChild.o.find(position-(mChildPosition+1));
                    }
                }

                mChildPosition+=1+mChild.o.mSize;

                if (mChild.next==null)
                {
                    //添加下一个
                    if (mChildCount==MAXITEM)
                    {
                        //如果个数满了，把第一个链接到最后一个成环
                        mChild.linkThisBefore(mStart);
                    }else
                    {
                        //new
                        mChildCount++;
                        mEnd=mChild.linkThisBefore(new ExpandableItem(mDepth+1,this));
                    }
                }
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
                        }

                        {
                            //更新
                            mChild.next.o.mIndex=mChild.o.mIndex+1;
                        }
                    }
                }

                mChild=mChild.next;
                if (mChild==mStart)
                {
                    mStart=mStart.next;
                    mEnd=mChild;
                }
            }
            //循环退出后说明当前child已经是目标
            saveChildPosition();
            return mChild.o;
        }else
        {
            while(mChildPosition!=position)
            {
                if (mChild.previous==null)
                {
                    //添加上一个
                    if (mChildCount==MAXITEM)
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
                        //如果旧的被展开则直接插入新的
                        if (mChild.previous.o.mSize!=0)
                        {
                            mChild.replaceThisPrevious(new LinkedNode<>(new ExpandableItem(mDepth+1,this)));
                        }

                        {
                            //更新
                            mChild.previous.o.mIndex=mChild.o.mIndex-1;
                        }
                    }

                    mChildPosition-=mChild.previous.o.mSize+1;

                    mChild=mChild.previous;
                    if (mChild==mEnd)
                    {
                        mEnd=mChild.previous;
                        mStart=mChild;
                    }

                    if (mChild.o.mSize!=0)
                    {
                        //上一child被展开，检查position处item是否位于上一child展开部分中
                        if (mChildPosition<position)
                        {
                            //先保存child位置
                            saveChildPosition();
                            return mChild.o.find(position-(mChildPosition+1));
                        }else if (mChildPosition==position)
                        {
                            saveChildPosition();
                            return mChild.o;
                        }
                    }
                }
            }
            //循环退出后说明当前child已经是目标
            saveChildPosition();
            return mChild.o;
        }
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
