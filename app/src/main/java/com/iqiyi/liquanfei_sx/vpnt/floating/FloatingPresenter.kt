package com.iqiyi.liquanfei_sx.vpnt.floating

import com.iqiyi.liquanfei_sx.vpnt.CommonPresenter

/**
 * Created by Administrator on 2017/12/12.
 */
open class FloatingPresenter: CommonPresenter() {
    private var mWindow:FloatingWindow?=null

    var window
        get() = mWindow
        set(value){mWindow=value}
}