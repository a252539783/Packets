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
    public void linkThisAfter(LinkedNode<T> node)
    {
        previous=node;

        if (node!=null) {
            node.next = this;
        }
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

        next.next.previous=node;
        next=node;

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
