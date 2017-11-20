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

    abstract String doRequest(String folder);

    static PersistRequest newWriteRequest(long time, PacketList list, Packet packet)
    {
        return new WriteRequest(time, list, packet);
    }

    public static PersistRequest newWriteSavedRequest(String name,long time,PacketList list,TCPPacket packet)
    {
        return new SaveRequest(name,time,list,packet);
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

    public static PersistRequest newReadSavedRequest()
    {
        return new LoadSavedRequest();
    }

    public static PersistRequest newReadSavedRequest(int uid)
    {
        return new LoadSavedRequest(uid);
    }

    static PersistRequest newCreateRequest(long time)
    {
        return new PersistRequest.CreateRequest(time);
    }

    static PersistRequest newCreateSavedRequest(int uid)
    {
        return new CreateSavedRequest(uid);
    }

    private static class CreateRequest extends PersistRequest
    {
        long mTime=0;

        CreateRequest(long time)
        {
            mTime=time;
        }

        @Override
        String doRequest(String folder) {
            String newF=Constants.PrivateFileLocation.HISTORY+File.separator+mTime+File.separator;
            new File(newF).mkdirs();

            return newF;
        }
    }

    private static class CreateSavedRequest extends PersistRequest
    {
        int mUid=0;

        CreateSavedRequest(int uid)
        {
            mUid=uid;
        }

        @Override
        String doRequest(String folder) {
            String newF=Constants.PrivateFileLocation.SAVED+File.separator+mUid+File.separator;
            new File(newF).mkdirs();

            return newF;
        }
    }

    private static class SaveRequest extends PersistRequest
    {
        String mName="";
        PacketList mList;
        TCPPacket mPacket;
        long mTime;

        private SaveRequest(String name,long time, PacketList list, TCPPacket packet)
        {
            mPacket=packet;
            mTime=time;
            mList =list;
            mName=name;
        }

        @Override
        String doRequest(String folder) {
            if (mName.equals(""))
                mName+=mTime;

            try {
                FileOutputStream fos=new FileOutputStream(
                        Constants.PrivateFileLocation .SAVED+File.separator+mList.mInfo.info.applicationInfo.uid+File.separator+mName+Constants.FileType._PACKETS,true);
                fos.write(ByteConvert.getLong(mTime));
                fos.write(mPacket.getRawData());
                fos.close();

                LocalPackets.get().initSavedPacket(mList.mInfo.info.applicationInfo.uid,mTime,mPacket);
            } catch (IOException e) {
                Log.e("xx:fileOutPut",e.toString());
            }

            return null;
        }
    }

    private static class WriteRequest extends PersistRequest
    {
        Packet mPacket;
        PacketList mList;
        long mTime;

        private WriteRequest(long time,PacketList list, Packet packet)
        {
            mPacket=packet;
            mTime=time;
            mList =list;
        }

        @Override
        String doRequest(String folder) {
            try {
                //.index+10000 是为了更方便的以时间（即文件名）进行排序
                FileOutputStream fos=new FileOutputStream(
                        folder+File.separator+ (mList.mIndex+1000000000)+'_'+mList.mInfo.info.applicationInfo.uid+Constants.FileType._PACKETS,true);
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
        String doRequest(String folder) {

            if (mIndex!=-1)
            {
                try {
                    LocalPackets.CaptureInfo ci=LocalPackets.get().mAllPackets.get(mTimeIndex);
                    FileInputStream fis=new FileInputStream(new File(Constants.PrivateFileLocation.HISTORY+File.separator+
                            ci.mTime+File.separator+(ci.mPackets.get(mIndex).mIndex+1000000000)+"_"+ci.mPackets.get(mIndex).mInfo.info.applicationInfo.uid+ Constants.FileType._PACKETS));

                    if (ci.mPackets.get(mIndex).size()>1)
                        return null;

                    long time=0;
                    byte []timeBuf=new byte[8];

                    PacketList.PacketItem pi=ci.mPackets.get(mIndex).get(0);
                    if (pi.mPacket.getIpInfo().length!=60)//抓包过程中出现问题
                    {
                        fis.skip(8);
                        byte[] src=new byte[pi.mPacket.getIpInfo().length];
                        IOUtil.read(fis,src);

                        pi.mPacket=(TCPPacket) new IPPacket(src).getData();
                    }else
                    {
                        fis.skip(68);       //第一个数据包已经获取过了
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
                        Thread.sleep(10);
                    }
                } catch (IOException | InterruptedException e) {
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
//                for (int i=0;i<files.length;i++)
//                {
//                    names[i]=names[i].substring(0,names[i].length()- Constants.FileType._PACKETS.length());
//                }
                Arrays.sort(names);
                for (int i=0;i<files.length;i++) {
                    files[i] = new File(base, names[i]);
                }

                    //File []files=new File(Constants.PrivateFileLocation.HISTORY+File.separator+ LocalPackets.get().mAllPackets.get(mTimeIndex).mTime).listFiles();
                FileInputStream fis;
                TCPPacket []packets=new TCPPacket[files.length];
                long time=0;
                for (int i=0;i<files.length;i++)
                {
                    /**
                     * 预计开始的syn包大小为60，由于这里只是要获取基本的ip信息和端口，
                     * 所以即使实际大小不正确也无所谓，在后面读取所有数据包时会再更新一次
                     */
                    byte []src=new byte[60];
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
                        int uid=Integer.parseInt(ss[1].substring(0,ss[1].length()- Constants.FileType._PACKETS.length()));

                        LocalPackets.get().initPackets(mTimeIndex,time,packets[i],listIndex,uid);
                    }catch (ClassCastException e)
                    {
                        //不是tcp，先忽略它们
                    }
                }
            }else
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

            return null;
        }
    }

    private static class LoadSavedRequest extends PersistRequest
    {

        int mUid=-1;

        private LoadSavedRequest(){

        }

        private LoadSavedRequest(int uid)
        {
            mUid=uid;
        }

        @Override
        String doRequest(String folder) {
            if (mUid==-1)
            {
                File file=new File(Constants.PrivateFileLocation.SAVED);
                if (file.exists())
                {
                    LocalPackets.get().initSavedList(file.list());
                }else
                {
                    LocalPackets.get().initHistory(null);
                }
            }else
            {
                LocalPackets.SavedInfo si=LocalPackets.get().getSavedInfo(mUid);
                if (si.mPackets.size()!=0)
                    return null;

                File base=new File(Constants.PrivateFileLocation.SAVED+File.separator+ mUid);
                String []names=base.list();
                long[] times=new long[names.length];
                File []files=new File[names.length];
                for (int i=0;i<files.length;i++)
                {
                    String ss[]=names[i].split(File.separator);
                    times[i]=Long.parseLong(ss[ss.length-1].split(".")[0]);
                }
                Arrays.sort(times);

                for (int i=0;i<files.length;i++)
                {
                    files[i]=new File(base,times[i]+ Constants.FileType._PACKETS);
                }

                //File []files=new File(Constants.PrivateFileLocation.HISTORY+File.separator+ LocalPackets.get().mAllPackets.get(mTimeIndex).mTime).listFiles();
                FileInputStream fis;
                TCPPacket []packets=new TCPPacket[files.length];
                long time=0;
                for (int i=0;i<files.length;i++)
                {
                    /**
                     * 只获取端口ip信息
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

                        LocalPackets.get().initSavedPacketUnchecked(i,time,packets[i]);
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
