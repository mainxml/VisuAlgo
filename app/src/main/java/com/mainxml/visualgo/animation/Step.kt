package com.mainxml.visualgo.animation

/**
 * 动画步骤状态
 * @param animationIndex Int 动画队列索引
 * @constructor
 */
class Step(val animationIndex: Int) {

    /** 动画前视图位置列表 List<Triple<视图下标, x轴, y轴>> */
    lateinit var beforeAnimationPositionList : List<Triple<Int, Float, Float>>

    /** 动画后视图位置列表 List<Triple<视图下标, x轴, y轴>> */
    var afterAnimationPositionList : List<Triple<Int, Float, Float>>? = null
}