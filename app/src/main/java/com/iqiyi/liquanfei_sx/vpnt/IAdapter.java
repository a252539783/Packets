package com.iqiyi.liquanfei_sx.vpnt;

/**
 * Created by Administrator on 2017/11/27.
 */

public interface IAdapter {
    void setSource(Object src);
    void removeListeners();
    void setListeners();
    void onFilterChanged();
}
