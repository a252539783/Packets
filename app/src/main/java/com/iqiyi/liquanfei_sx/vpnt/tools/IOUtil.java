package com.iqiyi.liquanfei_sx.vpnt.tools;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2017/11/13.
 */

public class IOUtil {

    public static void read(InputStream is,byte[] buf) throws IOException {
        read(is, buf,0,buf.length);
    }

    public static void read(InputStream is,byte[] buf,int offset,int length) throws IOException {
        if (is.available()<length)
            length=is.available();

        int end=offset+length;
        while (offset!=end)
        {
            offset+=is.read(buf,offset,end-offset);
        }
    }

}
