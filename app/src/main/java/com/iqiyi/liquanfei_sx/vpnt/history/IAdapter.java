package com.iqiyi.liquanfei_sx.vpnt.history;

import com.iqiyi.liquanfei_sx.vpnt.packet.LocalPackets;

import java.util.List;

/**
 * Created by Administrator on 2017/11/27.
 */

interface IAdapter {
    void setHistorySource(List<LocalPackets.CaptureInfo> src);
}
