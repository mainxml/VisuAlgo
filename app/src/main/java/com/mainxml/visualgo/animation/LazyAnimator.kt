package com.mainxml.visualgo.animation

import android.animation.Animator

/**
 * 懒加载动画
 * 对View进行属性动画时，View的属性会频繁的改变和复用，使用懒加载可使动画在时间线上同步的执行。
 * @author zcp
 */
typealias LazyAnimator = () -> Animator