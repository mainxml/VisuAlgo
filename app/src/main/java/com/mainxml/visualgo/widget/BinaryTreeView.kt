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
import java.util.LinkedList
import java.util.Queue
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

    /** 二叉树根节点 */
    private var treeRoot: Node? = null

    /**
     * 二叉树节点类
     */
    data class Node(
        val value: Int,
        var left: Node? = null,
        var right: Node? = null
    )

    /** 二叉树深度，根节点从0开始 */
    private var depth = 0

    /** 动画状态 */
    private var animationStatus = AnimationState.NONE

    /**
     * 动画状态枚举类
     */
    private enum class AnimationState {
        NONE, ADD_NODE_TAIL, SEARCH
    }

    init {
        // 标记用于绘制二叉树节点间的连接线
        setWillNotDraw(false)

        // xml预览
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

    /**
     * 添加节点
     * @param value Int? 节点值
     */
    fun add(value: Int? = null) {
        if (animationStatus != AnimationState.NONE) {
            return
        }
        animationStatus = AnimationState.ADD_NODE_TAIL
        val v = value ?: if (treeArray.isEmpty()) 0 else treeArray.last() + 1
        setTreeArray(treeArray.toMutableList().apply { add(v) }.toIntArray())
    }

    /** 搜索节点位置 */
    private var searchNodePoint = PointF()

    /**
     * 搜索节点
     * @param value Int
     */
    fun search(value: Int) {
        val rootNodeView = getChildAt(0) as VisualElement
        val radius = rootNodeView.measuredWidth / 2
        searchNodePoint = PointF(rootNodeView.x + radius, rootNodeView.y + radius)
        animationStatus = AnimationState.SEARCH

        invalidate()
    }

    /**
     * 获取左子树的下标
     * @param index Int
     * @return Int
     */
    private fun leftChildIndex(index: Int): Int {
        return 2 * index + 1
    }

    /**
     * 获取右子树的下标
     * @param index Int
     * @return Int
     */
    private fun rightChildIndex(index: Int): Int {
        return 2 * index + 2
    }

    /**
     * 数组转节点
     */
    private fun array2Node() {
        if (treeArray.isEmpty()) {
            return
        }

        val nodeList = mutableListOf<Node>()
        for (value in treeArray) {
            nodeList.add(Node(value))
        }

        treeRoot = nodeList[0]

        for (index in treeArray.indices) {
            val node = nodeList[index]

            val leftChildIndex = leftChildIndex(index)
            if (leftChildIndex in treeArray.indices) {
                node.left = nodeList[leftChildIndex]
            }

            val rightChildIndex = rightChildIndex(index)
            if (rightChildIndex in treeArray.indices) {
                node.right = nodeList[rightChildIndex]
            }
        }
    }

    /**
     * 节点转数组
     */
    private fun node2Array() {
        if (treeRoot == null) {
            return
        }

        val list = mutableListOf<Int>()

        // 广度优先遍历
        val queue: Queue<Node> = LinkedList()
        queue.offer(treeRoot)
        while (queue.isNotEmpty()) {
            val node = queue.poll()
            node?.value?.let { list.add(it)  }
            node?.left?.let { queue.offer(it) }
            node?.right?.let { queue.offer(it) }
        }

        setTreeArray(list.toIntArray())
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

    private val curNodePoint = PointF()
    private val childNodePoint = PointF()
    private val linePath = Path()
    private var linePaint: Paint = Paint().apply {
        isAntiAlias = true
        color = MyColor.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f.dp
    }
    private var searchPaint: Paint = Paint().apply {
        isAntiAlias = true
        color = MyColor.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 2f.dp
    }

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
            // 计算节点坐标位置并布局
            calculateNodePoint(index, childNodePoint)
            val cl = (childNodePoint.x - radius).toInt()
            val ct = (childNodePoint.y - radius).toInt()
            val cr = cl + childSize
            val cb = ct + childSize
            child.layout(cl, ct, cr, cb)
        }

        handleAnimation()
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
            val child = getChildAt(index) as VisualElement
            curNodePoint.x = child.x + child.measuredWidth / 2f
            curNodePoint.y = child.y + child.measuredHeight / 2f

            // 绘制左子节点连接线
            drawNodeLine(canvas, leftChildIndex(index))
            // 绘制右子节点连接线
            drawNodeLine(canvas, rightChildIndex(index))
        }

        if (animationStatus == AnimationState.SEARCH) {
            val rootNodeView = getChildAt(0) as VisualElement
            val radius = rootNodeView.measuredWidth / 2f + 2f.dp
            canvas.drawCircle(searchNodePoint.x, searchNodePoint.y, radius, searchPaint)
        }
    }

    private fun drawNodeLine(canvas: Canvas, childIndex: Int) {
        if (childIndex in treeArray.indices) {
            val childSize = getChildAt(0).measuredWidth
            val radius = childSize / 2

            // 获取子节点坐标位置
            val nodeView = getChildAt(childIndex) as VisualElement
            childNodePoint.x = nodeView.x + nodeView.measuredWidth / 2f
            childNodePoint.y = nodeView.y + nodeView.measuredHeight / 2f

            val angle = calculateAngle(curNodePoint, childNodePoint)

            val sx = curNodePoint.x + radius * cos(angle)
            val sy = curNodePoint.y + radius * sin(angle)
            val ex = childNodePoint.x
            val ey = childNodePoint.y - radius

            linePath.reset()
            linePath.moveTo(sx, sy)
            linePath.lineTo(ex, ey)
            canvas.drawPath(linePath, linePaint)
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

    private fun handleAnimation() {
        if (animationStatus == AnimationState.ADD_NODE_TAIL) {
            val child = getChildAt(childCount - 1) as VisualElement
            val childSize = child.measuredWidth
            val originX = child.x
            val originY = child.y

            val nl = width / 2
            val nt = child.y.toInt() + verticalSpacing
            val nr = nl + childSize
            val nb = nt + childSize
            child.layout(nl, nt, nr, nb)

            post {
                child.animate().apply {
                    x(originX)
                    y(originY)
                    setUpdateListener {
                        // 同时重绘连接线
                        invalidate()
                    }
                    setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            animationStatus = AnimationState.NONE
                        }
                    })
                    start()
                }
            }
        }
    }
}