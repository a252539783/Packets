package com.iqiyi.liquanfei_sx.vpnt.text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by Administrator on 2017/11/14.
 */

public class FileInfo {

    RandomAccessFile mFile;
    int length;
    long offset;

    public FileInfo(File file) throws IOException {
        mFile=new RandomAccessFile(file,"rw");
        length=(int)mFile.length();
        offset=0;
    }

    public void seek(long offset) throws IOException {
        if (this.offset!=offset) {
            mFile.seek(offset);
            this.offset=offset;
        }
    }

}
