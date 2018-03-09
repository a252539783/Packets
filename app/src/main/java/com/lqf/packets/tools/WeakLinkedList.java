package com.lqf.packets.tools;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Administrator on 2017/11/29.
 */

public class WeakLinkedList<E> extends AbstractSequentialList<E>
        implements List<E>, Deque<E>, Cloneable {

    private LinkedList<WeakReference<E>> mList;

    public WeakLinkedList()
    {
        mList=new LinkedList<>();
    }

    public WeakLinkedList(LinkedList<WeakReference<E>> list)
    {
        mList=list;
    }

    public E getFirst() {
        return mList.getFirst().get();
    }

    @Override
    public E getLast() {
        return mList.getLast().get();
    }

    @Override
    public E removeFirst() {
        return mList.removeFirst().get();
    }

    @Override
    public E removeLast() {
        return mList.removeLast().get();
    }

    @Override
    public void addFirst(E e) {
        mList.addFirst(new WeakReference<E>(e));
    }

    @Override
    public void addLast(E e) {
        mList.addLast(new WeakReference<E>(e));
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o)!=-1;
    }

    @Override
    public int size() {
        return mList.size();
    }

    @Override
    public boolean add(E e) {
        return mList.add(new WeakReference<E>(e));
    }

    @Override
    public boolean remove(Object o) {
        Iterator<WeakReference<E>> it=mList.listIterator();

        if (o==null)
        {
            while(it.hasNext())
            {
                WeakReference<E> e=it.next();
                if (e.get()==null)
                {
                    it.remove();
                    return true;
                }
            }
        }else
        {
            while(it.hasNext())
            {
                WeakReference<E> e=it.next();
                if (e.get()!=null&&e.get().equals(o))
                {
                    it.remove();
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size(),c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return mList.addAll(index,convert(c));
    }

    private Collection<WeakReference<E>> convert(Collection<? extends E> c)
    {
        ArrayList<WeakReference<E>> list=new ArrayList<>(c.size());
        Object[] array=c.toArray();

        for (Object o:array)
        {
            list.add(new WeakReference<E>((E)o));
        }
        return list;
    }

    @Override
    public void clear() {
        mList.clear();
    }

    @Override
    public E get(int index) {
        return mList.get(index).get();
    }

    @Override
    public E set(int index, E element) {
        return mList.set(index,new WeakReference<>(element)).get();
    }

    @Override
    public void add(int index, E element) {
        mList.add(index, new WeakReference<E>(element));
    }

    @Override
    public E remove(int index) {
        return mList.remove(index).get();
    }

    @Override
    public int indexOf(Object o) {
        Iterator<WeakReference<E>> it=mList.listIterator();
        int i=0;

        if (o==null)
        {
            while(it.hasNext())
            {
                WeakReference<E> e=it.next();
                if (e.get()==null)
                {
                    return i;
                }
                i++;
            }
        }else
        {
            while(it.hasNext())
            {
                WeakReference<E> e=it.next();
                if (e.get()!=null&&e.get().equals(o))
                {
                    return i;
                }
                i++;
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        ListIterator<WeakReference<E>> it=mList.listIterator(size());
        int i=0;

        if (o==null)
        {
            while(it.hasPrevious())
            {
                WeakReference<E> e=it.previous();
                if (e.get()==null)
                {
                    return i;
                }
                i++;
            }
        }else
        {
            while(it.hasPrevious())
            {
                WeakReference<E> e=it.previous();
                if (e.get()!=null&&e.get().equals(o))
                {
                    return i;
                }
                i++;
            }
        }

        return -1;
    }

    @Override
    public E peek() {
        return mList.peek().get();
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public E poll() {
        return mList.poll().get();
    }

    @Override
    public E remove() {
        return mList.remove().get();
    }

    @Override
    public boolean offer(E e) {
        return add(e);
    }

    @Override
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    @Override
    public E peekFirst() {
        return mList.peekFirst().get();
    }

    @Override
    public E peekLast() {
        return mList.peekLast().get();
    }

    @Override
    public E pollFirst() {
        return mList.pollFirst().get();
    }

    @Override
    public E pollLast() {
        return mList.pollLast().get();
    }

    @Override
    public void push(E e) {
        mList.push(new WeakReference<E>(e));
    }

    @Override
    public E pop() {
        return mList.pop().get();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        ListIterator<WeakReference<E>> it=mList.listIterator(size());

        if (o==null)
        {
            while(it.hasPrevious())
            {
                WeakReference<E> e=it.previous();
                if (e.get()==null)
                {
                    it.remove();
                    return true;
                }
            }
        }else
        {
            while(it.hasPrevious())
            {
                WeakReference<E> e=it.previous();
                if (e.get()!=null&&e.get().equals(o))
                {
                    it.remove();
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public ListIterator<E> listIterator() {
        return new ListItr(mList.listIterator());
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new ListItr(mList.listIterator(index));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    private class ListItr implements ListIterator<E>
    {
        private ListIterator<WeakReference<E>> mIt;
        ListItr(ListIterator it)
        {
            mIt=it;
        }

        @Override
        public boolean hasNext() {
            return mIt.hasNext();
        }

        @Override
        public E next() {
            return mIt.next().get();
        }

        @Override
        public boolean hasPrevious() {
            return mIt.hasPrevious();
        }

        @Override
        public E previous() {
            return mIt.previous().get();
        }

        @Override
        public int nextIndex() {
            return mIt.nextIndex();
        }

        @Override
        public int previousIndex() {
            return mIt.previousIndex();
        }

        @Override
        public void remove() {
            mIt.remove();
        }

        @Override
        public void set(E e) {
            mIt.set(new WeakReference<E>(e));
        }

        @Override
        public void add(E e) {
            mIt.add(new WeakReference<E>(e));
        }
    }

    private class DescendingIterator implements Iterator<E>
    {
        private final ListItr itr = new ListItr(mList.listIterator());
        public boolean hasNext() {
            return itr.hasPrevious();
        }
        public E next() {
            return itr.previous();
        }
        public void remove() {
            itr.remove();
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new WeakLinkedList<>((LinkedList<WeakReference<E>>) mList.clone());
    }

    @Override
    public Object[] toArray() {
        Object[] array=mList.toArray();
        for (int i=0;i<array.length;i++)
        {
            array[i]=((WeakReference<E>)array[i]).get();
        }

        return array;
    }

    @Override
    public <T> T[] toArray(@NonNull T[] a) {
        mList.toArray(a);

        Object[] array=a;
        for (int i=0;i<array.length;i++)
        {
            array[i]=((WeakReference<E>)array[i]).get();
        }

        return a;
    }
}
