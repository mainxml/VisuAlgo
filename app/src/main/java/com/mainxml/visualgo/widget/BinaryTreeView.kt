package com.mainxml.visualgo.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.mainxml.visualgo.util.MyColor
import com.mainxml.visualgo.util.dp
import com.mainxml.visualgo.util.getDisplayMetrics
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sin

/**
 * 二叉树视图
 */
class BinaryTreeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** 完全二叉树数组 */
    private var treeArray: IntArray? = null
    /** 二叉树的深度，根节点为0 */
    private var treeDepth = 0

    /** 节点半径 */
    private val radius = 12f.dp
    /** 所有节点所需要的最大宽度 */
    private var maxWidth = 0.0
    /** 水平间距 */
    private var horizontalSpacing = 6f.dp
    /** 垂直间距 */
    private val verticalSpacing = 40f.dp

    fun setTreeArray(treeArray: IntArray) {
        this.treeArray = treeArray
        treeDepth = log2(treeArray.size.toDouble()).toInt()
        // 最大深度的最多节点数
        val maxNodesInMaxDepth = 2.0.pow(treeDepth)

        // 所有节点所需要的最大宽度
        maxWidth = maxNodesInMaxDepth * radius * 2 +
                (maxNodesInMaxDepth + 1) * horizontalSpacing

        // 最大不超过屏幕宽度
        val screenWidth = getDisplayMetrics().widthPixels
        if (maxWidth > screenWidth) {
            maxWidth = screenWidth.toDouble()

            horizontalSpacing = (
                (maxWidth - (maxNodesInMaxDepth * radius * 2)) /
                (maxNodesInMaxDepth + 1)
            ).toFloat()
        }

        invalidate()
    }

    fun getTreeArray() = treeArray

    private var nodePaint: Paint = Paint().apply {
        isAntiAlias = true
        color = MyColor.GREEN
        style = Paint.Style.FILL
    }
    private var linePaint: Paint = Paint().apply {
        isAntiAlias = true
        color = MyColor.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f.dp
    }
    private var textPaint: Paint = Paint().apply {
        isAntiAlias = true
        color = MyColor.WHITE
        textSize = 12f.dp
    }
    private val fontMetrics = textPaint.fontMetrics
    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        val tree = treeArray
        if (tree == null || tree.isEmpty()) {
            return
        }
        val indices = tree.indices
        for (i in indices) {
            val nodePoint = calculateNodePoint(i)
            // 绘制节点
            canvas.drawCircle(nodePoint.x, nodePoint.y, radius, nodePaint)
            // 绘制左子节点连接线
            drawChildLine(canvas, nodePoint, leftChildIndex(i), indices)
            // 绘制右子节点连接线
            drawChildLine(canvas, nodePoint, rightChildIndex(i), indices)

            // 绘制节点值的文字
            val text = tree[i].toString()
            val textWidth = textPaint.measureText(text)
            val tx = nodePoint.x - textWidth / 2
            val ty = nodePoint.y - (fontMetrics.ascent + fontMetrics.descent) / 2
            canvas.drawText(text, tx, ty, textPaint)
        }
    }

    private fun drawChildLine(canvas: Canvas, nodePoint: PointF, childIndex: Int, indices: IntRange) {
        if (childIndex in indices) {
            val childNodePoint = calculateNodePoint(childIndex)
            val radian = calculateRadian(nodePoint, childNodePoint)

            val sx = nodePoint.x + radius * cos(radian)
            val sy = nodePoint.y + radius * sin(radian)
            val ex = childNodePoint.x
            val ey = childNodePoint.y - radius

            path.reset()
            path.moveTo(sx, sy)
            path.lineTo(ex, ey)
            canvas.drawPath(path, linePaint)
        }
    }

    private fun calculateNodePoint(i: Int): PointF {
        val depth = log2(i + 1.0).toInt()
        val depthMaxNodeCount = 2.0.pow(depth).toInt() // 当前深度最大节点数量 2 ^ depth
        val parentDepthMaxIndex = 2.0.pow(depth).toInt() - 1

        val nodeWidth = maxWidth / depthMaxNodeCount
        val relativeIndex = (i + 1) - parentDepthMaxIndex

        val x = (nodeWidth * relativeIndex - nodeWidth / 2).toFloat()
        val y = (depth + 1) * verticalSpacing

        return PointF(x, y)
    }

    private fun leftChildIndex(i: Int): Int {
        return 2 * i + 1
    }

    private fun rightChildIndex(i: Int): Int {
        return 2 * i + 2
    }

    /**
     * 计算两点之间和X轴的角度，单位为弧度
     */
    private fun calculateRadian(point1: PointF, point2: PointF): Float {
        return atan2((point2.y - point1.y), (point2.x - point1.x))
    }
}