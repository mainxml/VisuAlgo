package com.mainxml.visualgo.animation

import android.graphics.PointF

/**
 * 跟踪步骤
 * @param codeLineNumber Int 当前步骤代码行号
 * @param animationIndex Int 当前步骤动画索引
 * @param elementPoints List<PointF> 当前步骤所有元素的位置列表
 */
data class TrackStep(
    val codeLineNumber: Int,
    val animationIndex: Int,
    val elementPoints: List<PointF>
)