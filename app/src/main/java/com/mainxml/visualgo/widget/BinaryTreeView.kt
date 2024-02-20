package com.mainxml.visualgo.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.log2
import kotlin.math.pow

/**
 * 二叉树视图
 */
class BinaryTreeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val NODE_RADIUS = 30 // 节点的半径
    }

    /** 完全二叉树数组 */
    private var treeArray: IntArray? = null
    /** 二叉树的深度 */
    private var treeDepth = 0

    private var nodePaint: Paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    private var linePaint: Paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    fun setTreeArray(treeArray: IntArray) {
        this.treeArray = treeArray
        treeDepth = log2((treeArray.size + 1).toDouble()).toInt() // 计算树的深度
        invalidate() // 重新绘制
    }

    override fun onDraw(canvas: Canvas) {
        if (treeArray == null || treeArray?.size == 0) {
            return
        }

        // 计算节点的间距
        val horizontalSpacing = 100
        val verticalSpacing = 100

        // 遍历绘制节点和连接线
        for (i in treeArray!!.indices) {
            val level = log2((i + 1).toDouble()).toInt() + 1
            val x = ((i + 1 - 2.0.pow((level - 1).toDouble())) * horizontalSpacing + horizontalSpacing / 2).toInt()
            val y = level * verticalSpacing

            // 绘制节点
            canvas.drawCircle(x.toFloat(), y.toFloat(), NODE_RADIUS.toFloat(), nodePaint)

            // 绘制连接线和父节点
            if (i != 0) { // 不是根节点
                val parentIndex = (i - 1) / 2
                val parentLevel =
                    log2((parentIndex + 1).toDouble()).toInt() + 1
                val parentX = ((parentIndex + 1 - 2.0.pow((parentLevel - 1).toDouble())) * horizontalSpacing + horizontalSpacing / 2).toInt()
                val parentY = parentLevel * verticalSpacing

                // 绘制连接线
                val path = Path()
                path.moveTo(x.toFloat(), (y - NODE_RADIUS).toFloat())
                path.lineTo(parentX.toFloat(), (parentY + NODE_RADIUS).toFloat())
                canvas.drawPath(path, linePaint)

                // 绘制父节点
                canvas.drawCircle(
                    parentX.toFloat(),
                    parentY.toFloat(),
                    NODE_RADIUS.toFloat(),
                    nodePaint
                )
            }
        }
    }
}