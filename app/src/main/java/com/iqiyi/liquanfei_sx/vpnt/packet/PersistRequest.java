package com.iqiyi.liquanfei_sx.vpnt.packet;

import android.util.Log;

import com.iqiyi.liquanfei_sx.vpnt.Constants;
import com.iqiyi.liquanfei_sx.vpnt.tools.ByteConvert;
import com.iqiyi.liquanfei_sx.vpnt.tools.IOUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/11/8.
 */

public abstract class PersistRequest {

    abstract String doRequest();

    public static PersistRequest newWriteRequest(long time, LocalPackets.PacketList list, Packet packet)
    {
        return new WriteRequest(time, list, packet);
    }

    public static PersistRequest newReadRequest(int time,int index)
    {
        return new PersistRequest.LoadRequest(time, index);
    }

    public static PersistRequest newReadRequest(int time)
    {
        return new PersistRequest.LoadRequest(time);
    }

    public static PersistRequest newReadRequest()
    {
        return new PersistRequest.LoadRequest();
    }

    public static PersistRequest newCreateRequest(long time)
    {
        return new PersistRequest.CreateRequest(time);
    }

    private static class CreateRequest extends PersistRequest
    {
        long mTime=0;

        CreateRequest(long time)
        {
            mTime=time;
        }

        @Override
        String doRequest() {
            String newF=Constants.PrivateFileLocation.HISTORY+File.separator+mTime+File.separator;
            new File(newF).mkdirs();

            return newF;
        }
    }

    private static class WriteRequest extends PersistRequest
    {
        Packet mPacket;
        LocalPackets.PacketList mList;
        long mTime;

        private WriteRequest(long time, LocalPackets.PacketList list, Packet packet)
        {
            mPacket=packet;
            mTime=time;
            mList =list;
        }

        @Override
        String doRequest() {
            try {
                FileOutputStream fos=new FileOutputStream(Constants.PrivateFileLocation .HISTORY+File.separator+ (mList.mIndex+1000000000)+'_'+mList.mInfo.info.applicationInfo.uid,true);
                fos.write(ByteConvert.getLong(mTime));
                fos.write(mPacket.getRawData());
                fos.close();
            } catch (IOException e) {
                Log.e("xx:fileOutPut",e.toString());
            }

            return null;
        }
    }

    private static class LoadRequest extends PersistRequest
    {
        private static final int DEFAULT_BUFL=8196;
        private static byte[] mBuffer=new byte[DEFAULT_BUFL];

        private int mTimeIndex=-1;

        private int mIndex=-1;

        private LoadRequest()
        {
        }

        private LoadRequest(int time)
        {
            mTimeIndex =time;
        }

        private LoadRequest(int time,int index)
        {
            this(time);
            mIndex=index;
        }

        @Override
        String doRequest() {
            if (LocalPackets.get().mAllPackets==null)
            {
                File file=new File(Constants.PrivateFileLocation.HISTORY);
                if (file.exists())
                {
                    LocalPackets.get().initHistory(file.list());
                }else
                {
                    LocalPackets.get().initHistory(null);
                }
            }

            if (mIndex!=-1)
            {
                try {
                    LocalPackets.CaptureInfo ci=LocalPackets.get().mAllPackets.get(mTimeIndex);
                    FileInputStream fis=new FileInputStream(new File(Constants.PrivateFileLocation.HISTORY+File.separator+
                            ci.mTime+File.separator+(ci.mPackets.get(mIndex).mIndex+1000000000)+"_"+ci.mPackets.get(mIndex).mInfo.info.applicationInfo.uid));

                    if (ci.mPackets.get(mIndex).size()>1)
                        return null;

                    long time=0;
                    byte []timeBuf=new byte[8];

                    LocalPackets.PacketList.PacketItem pi=ci.mPackets.get(mIndex).get(0);
                    if (pi.mPacket.getIpInfo().length!=52)//抓包过程中出现问题
                    {
                        fis.skip(8);
                        byte[] src=new byte[pi.mPacket.getIpInfo().length];
                        IOUtil.read(fis,src);

                        pi.mPacket=(TCPPacket) new IPPacket(src).getData();
                    }else
                    {
                        fis.skip(60);       //第一个数据包已经获取过了
                    }

                    BufferedInputStream bis=new BufferedInputStream(fis);

                    byte[] src;
                    while ((bis.available())!=0)
                    {
                        IOUtil.read(bis,timeBuf);
                        time=ByteConvert.parseLong(timeBuf,0);

                        IOUtil.read(bis,mBuffer,0,4);
                        src=new byte[ByteConvert.parseInt(mBuffer,2,2)];
                        IOUtil.read(bis,mBuffer,4,src.length-4);
                        System.arraycopy(mBuffer,0,src,0,src.length);
                        TCPPacket packet=(TCPPacket) new IPPacket(src).getData();
                        LocalPackets.get().initPacketList(mTimeIndex,mIndex,time,packet,false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassCastException e)
                {
                    return null;        //说明不是tcp包，先忽略它们
                }
            }else if (mTimeIndex !=-1)
            {
                LocalPackets.CaptureInfo ci=LocalPackets.get().mAllPackets.get(mTimeIndex);
                if (ci.mPackets.size()!=0)
                    return null;

                File base=new File(Constants.PrivateFileLocation.HISTORY+File.separator+ ci.mTime);
                String []names=base.list();
                File []files=new File[names.length];
                for (int i=0;i<files.length;i++)
                {
                    files[i]=new File(base,names[i]);

                    String ss[]=names[i].split(File.separator);
                    names[i]=ss[ss.length-1];
                }
                Arrays.sort(names);

                //File []files=new File(Constants.PrivateFileLocation.HISTORY+File.separator+ LocalPackets.get().mAllPackets.get(mTimeIndex).mTime).listFiles();
                FileInputStream fis;
                TCPPacket []packets=new TCPPacket[files.length];
                long time=0;
                for (int i=0;i<files.length;i++)
                {
                    /**
                     * 每个数据包开始必然是本应用发出的SYN数据，长度52字节
                     */
                    byte []src=new byte[52];
                    int l=0;
                    try {
                        fis=new FileInputStream(files[i]);

                        IOUtil.read(fis,src,0,8);
                        time=ByteConvert.parseLong(src,0);
                        IOUtil.read(fis,src);
                    } catch (IOException e) {
                        continue;
                    }

                    try {
                        IPPacket ip=new IPPacket(src);
                        Log.e("xx",""+ip.length);
                        packets[i] = (TCPPacket) ip.getData();

                        String []ss=names[i].split("_");
                        int listIndex=Integer.parseInt(ss[0])-1000000000;
                        int uid=Integer.parseInt(ss[1]);

                        LocalPackets.get().initPackets(mTimeIndex,time,packets[i],listIndex,uid);
                    }catch (ClassCastException e)
                    {
                        //不是tcp，先忽略它们
                    }
                }
            }

            return null;
        }
    }

    public interface OnLoadHistoryListener
    {
        void loadOne(int index);

        void loadStart(List<LocalPackets.CaptureInfo> all);
    }
}
