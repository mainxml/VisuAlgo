package com.mainxml.visualgo.animation

import android.animation.Animator

/**
 * 懒加载动画
 *
 * 对View进行属性动画时，View的属性会频繁改变和复用，使用懒加载使动画在时间线上同步的执行。
 * @param indexes List<Int> 需要动画的View下标列表
 * @param create (indexes: List<Int>) -> Animator 延迟创建的动画函数
 * @author zcp
 */
data class LazyAnimator(
    val indexes: List<Int>,
    val create: (indexes: List<Int>) -> Animator
)