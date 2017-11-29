package com.iqiyi.liquanfei_sx.vpnt;

import com.iqiyi.liquanfei_sx.vpnt.packet.LocalPackets;

import java.util.List;

/**
 * Created by Administrator on 2017/11/27.
 */

public interface IAdapter {
    void setSource(Object src);
    void removeListeners();
    void setListeners();
}
