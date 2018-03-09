package com.lqf.packets;

/**
 * Created by Administrator on 2017/11/27.
 */

public interface IAdapter {
    void setSource(Object src);
    void removeListeners();
    void setListeners();
    void onFilterChanged();
}
