package com.iqiyi.liquanfei_sx.vpnt.tools;

import android.util.Log;

import com.iqiyi.liquanfei_sx.vpnt.service.ServerService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.Channel;
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

public abstract class NIOHandler<T> {

    private Selector mSelector;
    private Queue<Key> prepareRegister = new ConcurrentLinkedQueue<>();

    public NIOHandler() throws IOException {
        mSelector=Selector.open();
    }

    public void register(AbstractSelectableChannel channel,int ops,T o)
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
            SocketChannel channel = (SocketChannel) key.channel();
            ServerService.TCPStatus status = (ServerService.TCPStatus) key.attachment();
            try {
                if (key.isConnectable()) {
                    Log.e("xx", "key connect");
                    if (channel.isConnectionPending()) {
                        try {
                            if (channel.finishConnect()) {          //connect
                                key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                            }
                        } catch (IOException e) {
                            Log.e("xx","finishConnect error!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        }
                    }
                }
                if (key.isWritable()) {
                        //write
                }
                if (key.isValid() && key.isReadable()) {
                    //read
                }
            } catch (CancelledKeyException e) {
            }
        }
    }

    abstract void handleConnect();

    abstract void handleWrite();

    abstract void handleRead();

    class Key {
        AbstractSelectableChannel mChannel;
        int mOp;
        T mObject;

        Key(AbstractSelectableChannel c, int op, T o) {
            mChannel=c;
            mOp = op;
            mObject=o;
        }
    }
}
