package com.iqiyi.liquanfei_sx.vpnt.packet;

import android.util.Log;

import com.iqiyi.liquanfei_sx.vpnt.Constants;
import com.iqiyi.liquanfei_sx.vpnt.tools.ByteBufferPool;
import com.iqiyi.liquanfei_sx.vpnt.tools.ByteConvert;
import com.iqiyi.liquanfei_sx.vpnt.tools.NIOHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 2017/11/7.
 */

public class LocalPackets {

    public static LocalPacketsMgr get()
    {
        return LocalPacketsMgr.instance;
    }

    public static class WriteEntry
    {
        Packet mPacket;
        String mPkg;
        long mConnectTime;
        long mTime;

        public WriteEntry(String name,long time,long connectTime,Packet packet)
        {
            mPacket=packet;
            mPkg=name;
            mTime=time;
            mConnectTime=connectTime;
        }
    }

    public static class LocalPacketsMgr
    {
        static final LocalPacketsMgr instance=new LocalPacketsMgr();

        private PersistThread mThread;
        private long mCurrentTime;

        private LocalPacketsMgr()
        {
            mThread=new PersistThread();
            mThread.start();
        }

        static LocalPacketsMgr get()
        {
            return instance;
        }

        public void setTime(long time)
        {
            mCurrentTime=time;
        }

        public void addPersistPacket(WriteEntry entry)
        {
            mThread.mWriteQueue.add(entry);
            synchronized (this) {
                this.notify();
            }
        }

        private class PersistThread extends Thread
        {
            private ByteBufferPool mBufferPool=ByteBufferPool.getDefault();
            boolean mStart=false;
            private String mFolder;
            private Queue<WriteEntry> mWriteQueue=new ConcurrentLinkedQueue<>();

            @Override
            public synchronized void start() {
                mStart=true;
                super.start();
            }

            @Override
            public void run() {
                super.run();

                mFolder=new StringBuilder(Constants.PrivateFileLocation.HISTORY).append(File.separator)
                        .append(mCurrentTime).append(File.separator).toString();
                if (!new File(mFolder.toString()).mkdirs())
                {
                    return ;
                }
                WriteEntry p;
                FileOutputStream fos;
                while (mStart)
                {
                    while ((p=mWriteQueue.poll())!=null)
                    {
                        try {
                            fos=new FileOutputStream(mFolder+p.mConnectTime+p.mPkg,true);
                            fos.write(ByteConvert.getLong(p.mConnectTime));
                            fos.write(p.mPacket.getRawData());
                            fos.close();
                        } catch (FileNotFoundException e) {
                            Log.e("xx:fileOutPut",e.toString());
                        } catch (IOException e) {
                            Log.e("xx:fileOutPut",e.toString());
                        }
                    }

                    synchronized (instance)
                    {
                        try {
                            instance.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

}
