package com.iqiyi.liquanfei_sx.vpnt.tools;

/**
 * Created by Administrator on 2017/12/6.
 */

public class LinkedNode<T> {

    public LinkedNode<T> next=null,previous=null;
    public T o;

    public LinkedNode(T o)
    {
        this.o=o;
    }

    /**
     * link this after the node
     * @param node node
     */
    public LinkedNode<T> linkThisAfter(LinkedNode<T> node)
    {
        previous=node;

        if (node!=null) {
            node.next = this;
        }

        return node;
    }

    /**
     * deprecate next,and link node after this.
     * @param node node
     */
    public LinkedNode<T> replaceThisNext(LinkedNode<T> node)
    {
        if (node!=null)
        {
            node.next=next.next;
            node.previous=this;
        }

        if (next!=null&&next.next!=null)
        {
            next.next.previous=node;
        }
        next=node;

        return node;
    }

    public LinkedNode<T> replaceThisPrevious(LinkedNode<T> node)
    {
        if (node!=null)
        {
            node.next=this;
            node.previous=previous.previous;
        }

        if (previous!=null&&previous.previous!=null)
        {
            previous.previous.next=node;
        }
        previous=node;

        return node;
    }

    /**
     * link this before the node
     * @param node node
     */
    public LinkedNode<T> linkThisBefore(LinkedNode<T> node)
    {
        next=node;

        if (node!=null)
        {
            node.previous=this;
        }

        return node;
    }

    public void linkBetween(LinkedNode<T> before,LinkedNode<T> after)
    {
        linkThisAfter(before);
        linkThisBefore(after);
    }

}
