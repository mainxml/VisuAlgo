package com.mainxml.visualgo.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.mainxml.visualgo.util.MyColor
import com.mainxml.visualgo.util.dp

/**
 * 可视化元素
 * @author zcp
 */
class VisualElement @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class Type {
        /** 元素 */
        Element,
        /** 下标 */
        Index,
        /** 指针 */
        Point,
        /** 树节点 */
        TreeNode
    }

    companion object {
        fun createElement(context: Context, value: Int): VisualElement {
            return VisualElement(context).apply {
                type = Type.Element
                this.value = value
            }
        }

        fun createIndex(context: Context, index: Int): VisualElement {
            return VisualElement(context).apply {
                type = Type.Index
                value = index
            }
        }

        fun createPoint(context: Context, name: String): VisualElement {
            return createElement(context, 0).apply {
                type = Type.Point
                tag = name
            }
        }

        fun createTreeNode(context: Context, value: Int): VisualElement {
            return VisualElement(context).apply {
                type = Type.TreeNode
                this.value = value
            }
        }
    }

    /** 元素类型 */
    var type = Type.Element
        private set

    /** 元素的值 */
    var value: Int = 0

    /** 元素颜色 */
    @ColorInt
    var color = MyColor.GREEN
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 设置半径
     * @param radius Float
     */
    fun setRadius(radius: Int) {
        if (type != Type.TreeNode) {
            return
        }
        size = radius * 2
        invalidate()
    }

    /** 元素大小 */
    private var size: Int = if (isInEditMode) 72 else 24.dp

    private val paint = Paint().apply {
        isAntiAlias = true
        textSize = if (isInEditMode) 42f else 14f.dp
    }
    private val fontMetrics = paint.fontMetrics

    /**
     * 测量
     * 只为wrap_content提供默认大小。
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = resolveSize(size, widthMeasureSpec)
        val measuredHeight = resolveSize(size, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    /**
     * 布局
     * 此方法用于ViewGroup布局子View，子View留空
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        /* no-op */
    }

    /**
     * 绘制
     */
    override fun onDraw(canvas: Canvas) {
        // 根据类型绘制图形
        paint.color = when (type) {
            Type.Element,
            Type.TreeNode -> color
            Type.Index -> MyColor.GRAY
            Type.Point -> MyColor.CYAN
        }
        when (type) {
            Type.Element,
            Type.Index,
            Type.Point -> {
                // 绘制矩形
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
            Type.TreeNode -> {
                // 绘制圆形
                canvas.drawCircle(width / 2f, height / 2f, size / 2f, paint)
            }
        }

        // 绘制文字
        paint.color = MyColor.WHITE
        val text = if (type == Type.Point) { tag } else { value }.toString()
        val textWidth = paint.measureText(text)
        val tx = width / 2f - textWidth / 2
        val ty = height / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2
        canvas.drawText(text, tx, ty, paint)
    }
}