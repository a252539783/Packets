package com.iqiyi.liquanfei_sx.vpnt.tools;

import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Administrator on 2017/11/7.
 */

public abstract class NIOHandler<T extends NIOHandler.AttachmentObj> {

    private Selector mSelector;
    private Queue<Key> prepareRegister = new ConcurrentLinkedQueue<>();
    private ByteBufferPool mBufferPool=ByteBufferPool.getDefault();

    public NIOHandler() throws IOException {
        mSelector=Selector.open();
    }

    public void register(SocketChannel channel,int ops,T o)
    {
        prepareRegister.add(new Key(channel,ops,o));
    }

    public final void handle()
    {
        while (!prepareRegister.isEmpty()) {
            Key key;
            try {
                key = prepareRegister.poll();
            } catch (NoSuchElementException e) {
                break;
            }
            try {
                key.mChannel.register(mSelector, key.mOp, key.mObject);
            } catch (CancelledKeyException e) {

            } catch (ClosedChannelException e) {

            }
        }

        try {
            if (mSelector.select() <= 0)
                return;
        } catch (CancelledKeyException e) {
                //ignore
        } catch (IOException e) {
            Log.e("xx","selector is Stop!!!!!!!!!!!!!!!!!!!!!!!!!");
            //return false;
        }
        Iterator<SelectionKey> keys = mSelector.selectedKeys().iterator();
        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            keys.remove();
            SocketChannel channel =  (SocketChannel)key.channel();
            T obj = (T) key.attachment();
            try {
                if (key.isConnectable()) {
                    Log.e("xx", "key connect");
                    if (channel.isConnectionPending()) {
                        try {
                            if (channel.finishConnect()) {          //connect
                                afterConnect(obj);
                                key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                            }
                        } catch (IOException e) {
                            Log.e("xx","finishConnect error!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        }
                    }
                }

                if (key.isWritable()) {
                        //write
                    //handleWrite(obj);

                    if (!obj.mReadyWrite.isEmpty()) {
                        write:
                        while (obj.mReadyWrite.peek() != null) {
                            WriteEntry se =(WriteEntry) obj.mReadyWrite.peek();
                            if (!se.mAvailable) {
                                obj.mReadyWrite.poll();
                                continue;
                            }

                            /**说明该包还未被处理，下一轮继续判断*/
                            if (se.mReadyWrite == null) {
                                break;
                            }

                            ByteBuffer []bufs = se.mReadyWrite;

                            for (int i=0;i<bufs.length;i++)
                            {
                                if (bufs[i]==null)
                                {
                                    if (i==bufs.length-1)
                                    {
                                        obj.mReadyWrite.poll();
                                        afterWriteOne(obj,se);
                                        continue write;
                                    }
                                }else
                                {
                                    /**一部分数据写入完成  继续*/
                                    if (bufs[i].position()==bufs[i].limit())
                                    {
                                        mBufferPool.recycle(bufs[i]);
                                        bufs[i]=null;
                                    }else
                                    {
                                        try
                                        {
                                            /**实际转发*/
                                            channel.write(bufs[i]);
                                        }catch (Exception e)
                                        {

                                        }
                                    }
                                }
                            }
                            break;
                        }
                    } else {
                        /**转发队列为空，说明有转发任务已经完成,把OP_WRITE去掉，不然key会保留*/
                        key.interestOps((key.interestOps() | SelectionKey.OP_READ) & ~SelectionKey.OP_WRITE);
                    }

                }
                if (key.isValid() && key.isReadable()) {
                    //read
                    handleRead(channel,obj);
                }
            } catch (CancelledKeyException e) {
            }
        }
    }

    abstract void afterConnect(T object);

    abstract void handleWrite(AbstractSelectableChannel channel,T object);

    abstract void handleRead(AbstractSelectableChannel channel,T object);

    abstract void afterWriteOne(T object,WriteEntry entry);

    class Key {
        SocketChannel mChannel;
        int mOp;
        T mObject;

        Key(SocketChannel c, int op, T o) {
            mChannel=c;
            mOp = op;
            mObject=o;
        }
    }

    public static class AttachmentObj <E extends WriteEntry>
    {
        Queue<E> mReadyWrite;
        private ByteBuffer mReadBuffer;

        public AttachmentObj(int readBuf,boolean needWrite)
        {
            if (readBuf>0)
                mReadBuffer=ByteBuffer.allocate(readBuf);

            if (needWrite)
                mReadyWrite=new ConcurrentLinkedQueue<>();
        }

        public void addWrite(E src)
        {
            mReadyWrite.add(src);
        }
    }

    public static class WriteEntry
    {
        ByteBuffer[] mReadyWrite = null;
        boolean mAvailable = false;

        public WriteEntry(boolean avai)
        {
            mAvailable=avai;
        }

        public void setSource(ByteBuffer[] src)
        {
            mReadyWrite=src;
        }
    }
}
