package com.lqf.packets.floating

import com.lqf.packets.CommonPresenter

/**
 * Created by Administrator on 2017/12/12.
 */
open class FloatingPresenter: CommonPresenter() {
    private var mWindow:FloatingWindow?=null

    var window
        get() = mWindow
        set(value){mWindow=value}
}