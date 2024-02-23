package com.mainxml.visualgo.util

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue

/**
 * @author zcp
 */

val Int.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

val Float.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

fun getDisplayMetrics(): DisplayMetrics = Resources.getSystem().displayMetrics