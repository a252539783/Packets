package com.iqiyi.liquanfei_sx.vpnt;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by liquanfei_sx on 2017/8/15.
 */

public class TCPTransactor {

    private HashMap<Integer,Socket> mSockets=null;

    public TCPTransactor()
    {
        mSockets=new HashMap<>();
    }

    static class TCPStatus
    {

        Socket mSocket=null;

        public TCPStatus()
        {
        }
    }
}
