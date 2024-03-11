package com.mainxml.visualgo.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.view.children
import com.mainxml.visualgo.util.MyColor
import com.mainxml.visualgo.util.dp
import com.mainxml.visualgo.util.getScreenWidth
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
) : ViewGroup(context, attrs, defStyleAttr) {

    /** 二叉树节点数组 */
    private var treeArray = intArrayOf()

    /** 二叉树深度，根节点从0开始 */
    private var depth = 0

    /** 是否播放动画 */
    private var playAnimation = false

    init {
        // 用于绘制二叉树之间的连接线
        setWillNotDraw(false)

        // 预览
        if (isInEditMode) {
            setTreeArray(intArrayOf(1, 2, 3))
        }
    }

    /**
     * 设置二叉树
     * @param treeArray IntArray
     */
    fun setTreeArray(treeArray: IntArray) {
        this.treeArray = treeArray
        depth = log2(treeArray.size.toDouble()).toInt()

        removeAllViews()
        treeArray.forEach { value ->
            addView(VisualElement.createTreeNode(context, value))
        }
    }

    /** 获取二叉树节点数组 */
    fun getTreeArray() = treeArray

    /**
     * 添加节点
     * @param value Int?
     */
    fun add(value: Int? = null) {
        if (playAnimation) {
            return
        }
        playAnimation = true
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
    //                             绘制、布局、绘制
    // ---------------------------------------------------------------------

    /** 所有节点所需要的最大绘制宽度 */
    private var maxDrawWidth = 0
    /** 水平间距 */
    private var horizontalSpacing = 6.dp
    /** 垂直间距 */
    private val verticalSpacing = 40.dp

    private var linePaint: Paint = Paint().apply {
        isAntiAlias = true
        color = MyColor.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f.dp
    }
    private val curNodePoint = PointF()
    private val childNodePoint = PointF()
    private val path = Path()

    /**
     * 测量
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        children.forEach {
            if (it !is VisualElement) {
                throw IllegalArgumentException("子View必须为VisualElement类型")
            }
        }

        // 测量所有子View
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        // 获取测量后的子View大小，每个子View尺寸都相同
        val childSize = getChildAt(0).measuredWidth

        // 计算最大深度最多节点数
        val maxDepthMaxNodeCount = 2.0.pow(depth).toInt()
        // 计算所有节点所需的最大绘制宽度
        maxDrawWidth = maxDepthMaxNodeCount * childSize +
                (maxDepthMaxNodeCount + 1) * horizontalSpacing

        // 最大不超过屏幕宽度
        val screenWidth = getScreenWidth()
        if (maxDrawWidth > screenWidth) {
            maxDrawWidth = screenWidth
            horizontalSpacing = (maxDrawWidth - (maxDepthMaxNodeCount * childSize)) /
                    (maxDepthMaxNodeCount + 1)
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * 布局子View
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childSize = getChildAt(0).measuredWidth
        val radius = childSize / 2

        children.forEachIndexed { index, child ->
            calculateNodePoint(index, childNodePoint)

            val cl: Int
            val ct: Int
            if (playAnimation && index == childCount - 1) {
                // 最后一项时做点动画，起始位置放远点
                cl = this.width / 2
                ct = (childNodePoint.y - radius).toInt() + verticalSpacing
            } else {
                cl = (childNodePoint.x - radius).toInt()
                ct = (childNodePoint.y - radius).toInt()
            }
            val cr = cl + childSize
            val cb = ct + childSize
            child.layout(cl, ct, cr, cb)

            // 最后一项时做点动画，移动回对应的位置
            if (playAnimation && index == childCount - 1) {
                val targetX = childNodePoint.x - radius
                val targetY = childNodePoint.y - radius
                postDelayed({
                    child.animate().x(targetX).y(targetY)
                        .setUpdateListener {
                            // 同时重绘连接线
                            this@BinaryTreeView.invalidate()
                        }
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                playAnimation = false
                            }
                        })
                        .start()
                }, 300)
            }
        }
    }

    /**
     * 绘制节点间的连接线
     */
    override fun onDraw(canvas: Canvas) {
        if (treeArray.isEmpty()) {
            return
        }
        for (index in treeArray.indices) {
            // 获取当前节点坐标位置
            //calculateNodePoint(index, curNodePoint)
            val nodeView = getChildAt(index) as VisualElement
            curNodePoint.x = nodeView.x + nodeView.measuredWidth / 2f
            curNodePoint.y = nodeView.y + nodeView.measuredHeight / 2f

            // 绘制左子节点连接线
            drawNodeLine(canvas, leftChildIndex(index))
            // 绘制右子节点连接线
            drawNodeLine(canvas, rightChildIndex(index))
        }
    }

    private fun drawNodeLine(canvas: Canvas, childIndex: Int) {
        val childSize = getChildAt(0).measuredWidth
        val radius = childSize / 2

        if (childIndex in treeArray.indices) {
            // 获取子节点坐标位置
            //calculateNodePoint(childIndex, childNodePoint)
            val nodeView = getChildAt(childIndex) as VisualElement
            childNodePoint.x = nodeView.x + nodeView.measuredWidth / 2f
            childNodePoint.y = nodeView.y + nodeView.measuredHeight / 2f

            val angle = calculateAngle(curNodePoint, childNodePoint)

            val sx = curNodePoint.x + radius * cos(angle)
            val sy = curNodePoint.y + radius * sin(angle)
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

        val nodeWidth = maxDrawWidth / curDepthMaxNodeCount
        val relativeIndex = (index + 1) - parentDepthMaxIndex

        val x = (nodeWidth * relativeIndex - nodeWidth / 2).toFloat()
        val y = (curDepth + 1f) * verticalSpacing

        nodePoint.x = x
        nodePoint.y = y
    }

    /**
     * 计算两点之间和X轴的角度，单位为弧度
     */
    private fun calculateAngle(point1: PointF, point2: PointF): Float {
        return atan2((point2.y - point1.y), (point2.x - point1.x))
    }
}