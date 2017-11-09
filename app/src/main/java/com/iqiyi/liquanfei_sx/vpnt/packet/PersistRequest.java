package com.iqiyi.liquanfei_sx.vpnt.packet;

import android.util.Log;

import com.iqiyi.liquanfei_sx.vpnt.Constants;
import com.iqiyi.liquanfei_sx.vpnt.tools.ByteConvert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2017/11/8.
 */

public abstract class PersistRequest {

    abstract String doRequest(String folder);

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
        String doRequest(String folder) {
            String newF=folder+mTime+File.separator;
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
        String doRequest(String folder) {
            try {
                FileOutputStream fos=new FileOutputStream(folder+ mList.mIndex+'_'+mList.mInfo.info.applicationInfo.uid,true);
                fos.write(ByteConvert.getLong(mTime));
                fos.write(mPacket.getRawData());
                fos.close();
            } catch (FileNotFoundException e) {
                Log.e("xx:fileOutPut",e.toString());
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
                            ci.mTime+File.separator+ci.mPackets.get(mIndex).get(0).mTime));

                    if (ci.mPackets.get(mIndex).size()>1)
                        return null;

                    int avai;
                    int len=0;
                    int bufPosition=0;
                    int srcPosition=0;

                    int index=1;
                    fis.skip(48);       //第一个数据包已经获取过了

                    byte[] src=null;
                    while ((avai=fis.available())!=0)
                    {
                        if (avai>DEFAULT_BUFL)
                        {
                            avai=DEFAULT_BUFL;
                        }

                        len=fis.read(mBuffer,0,avai);
                        while (len!=0)
                        {
                            if (src==null)
                                src=new byte[(0xFFFF&mBuffer[bufPosition+2])<<8|(mBuffer[bufPosition+3]&0xff)];

                            /*
                            要读取的数据包较大，将buffer所有内容放入目标，移动目标标记，
                            重置buffer标记，退出当前循环继续读取文件
                             */
                            if (src.length-srcPosition>len)
                            {
                                System.arraycopy(mBuffer,bufPosition,src,srcPosition,len);
                                bufPosition=0;
                                srcPosition+=len;
                                break;
                            }

                            /*
                             * 要读取的数据包较小，从缓冲区拿出对应大小数据后移动缓冲区标记，
                             * 继续进行读取
                             */
                            if (src.length-srcPosition<=len)
                            {
                                System.arraycopy(mBuffer,bufPosition,src,srcPosition,src.length-srcPosition);
                                bufPosition+=src.length;

                                TCPPacket packet=(TCPPacket) new IPPacket(src).getData();
                                LocalPackets.get().initPacketList(mTimeIndex,mIndex,packet,false);

                                src=null;   //当前循环内继续读取数据
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if (mTimeIndex !=-1)
            {
                String []names=new File(Constants.PrivateFileLocation.HISTORY+File.separator+ LocalPackets.get().mAllPackets.get(mTimeIndex).mTime).list();
                File []files=new File[names.length];
                for (int i=0;i<files.length;i++)
                {
                    files[i]=new File(names[i]);

                    String ss[]=names[i].split(File.separator);
                    names[i]=ss[ss.length-1];
                }

                //File []files=new File(Constants.PrivateFileLocation.HISTORY+File.separator+ LocalPackets.get().mAllPackets.get(mTimeIndex).mTime).listFiles();
                FileInputStream fis;
                TCPPacket []packets=new TCPPacket[files.length];
                for (int i=0;i<files.length;i++)
                {
                    /**
                     * 每个数据包开始必然是本应用发出的SYN数据，长度40字节
                     */
                    byte []src=new byte[40];
                    int l=0;
                    try {
                        fis=new FileInputStream(files[i]);
                        fis.skip(8);
                        while ((l+=fis.read(src,l,40-l))!=40);
                    } catch (IOException e) {
                        continue;
                    }

                    try {
                        IPPacket ip=new IPPacket(src);
                        Log.e("xx",""+ip.length);
                        packets[i] = (TCPPacket) ip.getData();

                        String []ss=names[i].split("_");
                        int listIndex=Integer.parseInt(ss[0]);
                        int uid=Integer.parseInt(ss[1]);

                        LocalPackets.get().initPackets(mTimeIndex,packets[i],listIndex,uid);
                    }catch (ClassCastException e)
                    {
                        continue;
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
