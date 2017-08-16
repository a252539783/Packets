package com.iqiyi.liquanfei_sx.vpnt;

import java.nio.ByteBuffer;

/**
 * Created by liquanfei_sx on 2017/8/11.
 */

public class PacketTransactor {

    private ClientService mServer=null;

    public PacketTransactor(ClientService server) {
        mServer = server;
    }

    public void transact(ByteBuffer buffer)
    {

    }
}
