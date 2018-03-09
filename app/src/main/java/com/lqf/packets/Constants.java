package com.lqf.packets;

import android.content.Context;

import java.io.File;

/**
 * Created by Administrator on 2017/11/7.
 */

public class Constants {

    private static String FILES="null";

    public static void init(Context c)
    {
        FILES=c.getFilesDir().getAbsolutePath();
    }

    public static class PrivateFileLocation
    {
        public static final String PACKETS=FILES+ File.separator+"packets";
        public static final String SAVED=PACKETS+ File.separator+"saved";
        public static final String HISTORY=PACKETS+ File.separator+"history";
    }

    public static class FileType
    {
        public static final String PACKET="pkt";
        public static final String PACKETS="pkts";
        public static final String _PACKET=".pkt";
        public static final String _PACKETS=".pkts";
    }
}
