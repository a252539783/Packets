package com.lqf.packets.view;

import android.util.Log;

import com.lqf.packets.tools.LinkedNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    static final int MAXITEM=5;

    int mIndex=0;
    int mSize=0;
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

    /**
     * item大小变化时，应当且必须通知parent大小也改变
     * @param size 大小相对于以前的改变了多少,可以为负数
     */
    void sizeChange(int size)
    {
        mSize+=size;

        if (mParent!=null)
        {
            mParent.sizeChange(size);
        }
    }

    /**
     * 每个child的大小改变时，都应该去通知parent去改变childPosition
     * @param index parent的第index个child发生了大小变化
     * @param size 变化大小
     */
    void offsetChild(int index, int size)
    {
        if (mChild!=null&&mChild.o.mIndex>index)
        {
            mChildPosition+=size;
        }

        if (mParent!=null)
        {
            mParent.offsetChild(mIndex,size);
        }
    }

    int getRealPosition(int depth,int ...position)
    {
        if (depth<0||depth>=position.length)
            return 0;

        int res=position[depth];
        for (Map.Entry<Integer,LinkedNode<ExpandableItem>> entry:mExpands.entrySet())
        {
            if (entry.getKey()<position[depth])
                res+=entry.getValue().o.mSize;
        }

        ExpandableItem ei=findExpand(position[depth]);
        return ei==null?res:res+ei.getRealPosition(depth+1,position)+1;
    }

    /**
     * 从一个绝对位置去展开一个item
     * @param position 绝对位置;如果为-1表示当前item要被展开
     * @param initSize 展开的初始大小
     * @return true当被展开，false被跟着跌
     */
    int expand(int position,int initSize)
    {
        int oldSize=mSize;

        if (position==-1)
        {
            int d=0;
            boolean expand=false;

            if (mParent.mExpands.containsKey(mIndex))
            {
                //已经被展开,折叠
                d=-mSize;
                fresh(0);

                mParent.removeExpand(mIndex);
            }else
            {
                if (mParent!=null&&
                        //parent不是顶层，要保证parent已经被展开
                        ((mParent.mDepth!=-1&&mParent.mParent!=null&&mParent.mParent.mExpands.containsKey(mParent.mIndex))
                                ||mParent.mDepth==-1))//而对于顶层parent无须判断
                {
                    d=initSize-mSize;
                    fresh(initSize);

                    //通知parent
                    mParent.addExpand(mIndex,this);
                    expand=true;
                }
            }

            mStart=mEnd=mChild=null;
            mChildPosition=0;
            if (d!=0)
            {
                offsetChild(initSize,d);
                //mParent.sizeChange(d);
            }
            return expand?-1:oldSize;
        }

        ExpandableItem ei=find(position);
        if (ei!=null)
            return ei.expand(-1,initSize);
        else
            return 0;
    }

    /**
     * 在当前层次中的index位置插入一个;
     * 不会去根据index去计算扩展层次的实际位置，实际使用应该findExpand().insert()
     * @param index 当前层次相对位置
     */
    void insert(int index)
    {

        if (mStart==null)
        {
            //还没有查找过任何一次，此时没有缓存任何条目，所以不用管
        }else
        if (index<=mStart.o.mIndex)
        {
            //在缓存区前插入，所有缓存条目增加一即可
            LinkedNode<ExpandableItem> ei=mStart.next;
            mStart.o.mIndex++;
            while(ei!=mStart&&ei!=null)
            {
                ei.o.mIndex++;
                ei=ei.next;
            }

            //插入一定引起childPosition的改变
            mChildPosition++;
        }else if (index<=mEnd.o.mIndex)
        {
            //插入可能引起childPosition的改变
            if (mChild.o.mIndex<=index)
            {
                mChildPosition++;
            }

            //在缓存中插入，之后缓存条目需要后移
            LinkedNode<ExpandableItem> ei=mStart;
            for (int i=mStart.o.mIndex;i!=index;i++)
            {
                ei=ei.next;
            }
            //在新位置插入一个新的
            new LinkedNode<>(new ExpandableItem(mDepth+1,this)).linkBetween(ei.previous,ei);
            ei.previous.o.mIndex=ei.o.mIndex;

            //同时后面index自增
            while(ei!=mEnd)
            {
                ei.o.mIndex++;
                ei=ei.next;
            }

            //原先的最后一个会被丢弃，此时更新child为倒数第二个
            if (mChild==mEnd)
            {
                mChild=mEnd.previous;
                mChildPosition--;
            }

            //丢弃原先的最后一个
            mEnd=mEnd.previous;
            mEnd.linkThisBefore(mStart);
        }else
        {
            //后面插入的话管都不用管
        }

        //更新所有的展开条目
        LinkedList<LinkedNode<ExpandableItem>> list=new LinkedList<>();
        for (Map.Entry<Integer,LinkedNode<ExpandableItem>> entry:mExpands.entrySet())
        {
            if (entry.getKey()>=index)
            {
                //如果key与value不相等，说明已经在前面被更新过
                if (entry.getValue().o.mIndex==entry.getKey())
                {
                    entry.getValue().o.mIndex++;
                }
                list.add(entry.getValue());
                mExpands.remove(entry.getKey());
            }
        }
        while(!list.isEmpty())
        {
            LinkedNode<ExpandableItem> ei=list.pollFirst();
            mExpands.put(ei.o.mIndex,ei);
        }

        mSize++;
        if (mParent!=null) {
            mParent.sizeChange(1);
            offsetChild(index,1);
        }
    }

    /**
     * 用于某个item被展开时，通知parent自己被展开
     * @param index 被展开item的在parent中的相对位置
     * @param ei 被展开的item
     */
    private void addExpand(int index,ExpandableItem ei)
    {
        mExpands.put(index,new LinkedNode<>(ei));
    }

    private void removeExpand(int index)
    {
        mExpands.remove(index);
    }

    /**
     * 更新大小，并且丢失所有的扩展信息
     * (无法保留展开的条目是因为在更新时无法确认数据绑定的关系)
     * @param size 更新后的大小
     */
    void fresh(int size)
    {
        mExpands.clear();
        mChildPosition=0;
        mChildCount=0;
        mChild=mStart=mEnd=null;

        if (mParent!=null&&size-mSize!=0) {
            mParent.sizeChange(size - mSize);
        }
        mSize=size;
    }

    void freshExpandChildren(int depth)
    {
        if (depth==mDepth+1)
        {
            int oldSize=mSize;
            fresh(0);
            offsetChild(mIndex,-oldSize);
        }else if (depth>mDepth+1)
        {
            for (Map.Entry<Integer,LinkedNode<ExpandableItem>> entry:mExpands.entrySet())
            {
                entry.getValue().o.freshExpandChildren(depth);
            }
        }
    }

    /**
     * 用绝对位置去得到一组相对位置
     *
     * 得到的相对位置会很快的失效（当再次调用get(int)/find(int)时)
     * @param position 绝对位置
     * @return 相对位置数组，数组长度表示位置深度，第i个元素表示
     * 深度为i时其相对于parent的位置
     */
    int[] get(int position)
    {
        return mCachedPosition.get(find(position).mDepth);
    }

    /**
     * 直接使用相对位置来得到一个展开的child
     */
    ExpandableItem findExpand(int index)
    {
        LinkedNode<ExpandableItem> ei=mExpands.get(index);
        return ei==null?null:ei.o;
    }

    /**
     * 使用绝对位置来得到一个child
     */
    ExpandableItem find(int position)
    {
        if (position>=mSize||position<0) {
            Log.e("xx","find unknown position"+position);
            return null;
        }

        if (mChild==null)
        {
            mEnd=mStart=mChild=new LinkedNode<>(new ExpandableItem(mDepth+1,this));
        }

        if (mChildPosition==position)
        {
            //当前child就是目标
            //mEnd=mChild;
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
                        //mEnd=mChild;
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

                        //如果替换的下一个刚好是start，同时更新start
                        if (mChild==mStart.previous)
                        {
                            mStart=mChild.next;
                        }
                    }else
                    {
                        //下一个不是被展开的
                        //如果旧的被展开则插入新的
                        if (mChild.next!=null&&mExpands.get(mChild.next.o.mIndex)!=null)
                        {
                            mChild.replaceThisNext(new LinkedNode<>(new ExpandableItem(mDepth+1,this)));

                            //如果替换的下一个刚好是start，同时更新start
                            if (mChild==mStart.previous)
                            {
                                mStart=mChild.next;
                            }
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
                        //mStart.o.mIndex=mChild.o.mIndex-1;
                    }

                    //mChildPosition--;
                }
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

                        //如果替换的上一个刚好是end，同时更新end
                        if (mChild==mEnd.next)
                        {
                            mEnd=mChild.previous;
                        }
                    }else
                    {
                        //上一个不是被展开的
                        //如果旧的被展开则直接插入新的
                        if (mChild.previous!=null&&mExpands.get(mChild.previous.o.mIndex)!=null)
                        {
                            mChild.replaceThisPrevious(new LinkedNode<>(new ExpandableItem(mDepth+1,this)));

                            //如果替换的上一个刚好是end，同时更新end
                            if (mChild==mEnd.next)
                            {
                                mEnd=mChild.previous;
                            }
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

    /**
     * 保存并返回当前child的相对位置
     */
    private int[] saveChildPosition()
    {
        if (mChild==null)
            return new int[0];

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
