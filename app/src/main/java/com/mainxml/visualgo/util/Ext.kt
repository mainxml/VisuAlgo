package com.mainxml.visualgo.util

import com.mainxml.visualgo.base.BaseApplication

/**
 * @author zcp
 */

val Int.dp
    get() = (BaseApplication.get().resources.displayMetrics.density * this + 0.5f).toInt()

