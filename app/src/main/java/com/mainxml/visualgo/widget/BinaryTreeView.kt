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

    /** 二叉树节点数组 */
    private var treeArray = intArrayOf()
    /** 二叉树的深度，根节点为0开始 */
    private var depth = 0

    /** 节点半径 */
    private val radius = 12f.dp
    /** 所有节点所需要的最大宽度 */
    private var maxWidth = 0
    /** 水平间距 */
    private var horizontalSpacing = 6.dp
    /** 垂直间距 */
    private val verticalSpacing = 40.dp

    fun setTreeArray(treeArray: IntArray) {
        this.treeArray = treeArray
        depth = log2(treeArray.size.toDouble()).toInt()

        // 最大深度的最多节点数
        val maxNodesInMaxDepth = 2.0.pow(depth).toInt()
        // 所有节点所需要的最大宽度
        maxWidth = (
            maxNodesInMaxDepth * radius * 2 +
            (maxNodesInMaxDepth + 1) * horizontalSpacing
        ).toInt()
        // 最大不超过屏幕宽度
        val screenWidth = getDisplayMetrics().widthPixels
        if (maxWidth > screenWidth) {
            maxWidth = screenWidth
            horizontalSpacing = (
                (maxWidth - (maxNodesInMaxDepth * radius * 2)) / (maxNodesInMaxDepth + 1)
            ).toInt()
        }

        invalidate()
    }

    /** 获取二叉树节点数组 */
    fun getTreeArray() = treeArray

    /**
     * 添加节点
     * @param value Int?
     */
    fun add(value: Int? = null) {
        val v = value ?: run {
            if (treeArray.isEmpty()) 0 else treeArray.last() + 1
        }
        setTreeArray(treeArray.toMutableList().apply { add(v) }.toIntArray())
    }

    private fun leftChildIndex(i: Int): Int {
        return 2 * i + 1
    }

    private fun rightChildIndex(i: Int): Int {
        return 2 * i + 2
    }

    // ---------------------------------------------------------------------
    //                                  绘制
    // ---------------------------------------------------------------------

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

    private val curNodePoint = PointF()
    private val leftNodePoint = PointF()
    private val rightNodePoint = PointF()

    override fun onDraw(canvas: Canvas) {
        if (treeArray.isEmpty()) {
            return
        }
        val indices = treeArray.indices
        for (index in indices) {
            // 绘制当前节点
            drawNode(canvas, index, curNodePoint)
            // 绘制左子节点连接线
            drawNodeLine(canvas, leftChildIndex(index), leftNodePoint)
            // 绘制右子节点连接线
            drawNodeLine(canvas, rightChildIndex(index), rightNodePoint)
        }
    }

    private fun drawNode(canvas: Canvas, index: Int, curNodePoint: PointF) {
        calculateNodePoint(index, curNodePoint)
        canvas.drawCircle(curNodePoint.x, curNodePoint.y, radius, nodePaint)

        val text = treeArray[index].toString()
        val textWidth = textPaint.measureText(text)
        val tx = curNodePoint.x - textWidth / 2
        val ty = curNodePoint.y - (fontMetrics.ascent + fontMetrics.descent) / 2
        canvas.drawText(text, tx, ty, textPaint)
    }

    private fun drawNodeLine(canvas: Canvas, childIndex: Int, childNodePoint: PointF) {
        if (childIndex in treeArray.indices) {
            calculateNodePoint(childIndex, childNodePoint)
            val radian = calculateRadian(curNodePoint, childNodePoint)

            val sx = curNodePoint.x + radius * cos(radian)
            val sy = curNodePoint.y + radius * sin(radian)
            val ex = childNodePoint.x
            val ey = childNodePoint.y - radius

            path.reset()
            path.moveTo(sx, sy)
            path.lineTo(ex, ey)
            canvas.drawPath(path, linePaint)
        }
    }

    private fun calculateNodePoint(index: Int, nodePoint: PointF) {
        val curDepth = log2(index + 1.0).toInt()
        val curDepthMaxNodeCount = 2.0.pow(curDepth).toInt()
        val parentDepthMaxIndex = 2.0.pow(curDepth).toInt() - 1

        val nodeWidth = maxWidth / curDepthMaxNodeCount
        val relativeIndex = (index + 1) - parentDepthMaxIndex

        val x = (nodeWidth * relativeIndex - nodeWidth / 2).toFloat()
        val y = (curDepth + 1f) * verticalSpacing

        nodePoint.x = x
        nodePoint.y = y
    }

    /**
     * 计算两点之间和X轴的角度，单位为弧度
     */
    private fun calculateRadian(point1: PointF, point2: PointF): Float {
        return atan2((point2.y - point1.y), (point2.x - point1.x))
    }
}