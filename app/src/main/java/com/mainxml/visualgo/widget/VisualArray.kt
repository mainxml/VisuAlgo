package com.mainxml.visualgo.widget

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.view.children
import androidx.core.view.get
import com.mainxml.visualgo.util.MyColor
import com.mainxml.visualgo.util.dp

/**
 * 可视化数组
 * @author zcp
 */
class VisualArray @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    // region 测量布局绘制
    /** 边框Padding */
    private val edgePadding = if (isInEditMode) 12 else 4.dp
    /** 元素Padding */
    private val elementPadding = if (isInEditMode) 6 else 2.dp
    /** 边框圆角值 */
    private val corner = 0f
    /** 画笔线宽 */
    private val strokeWidth = if (isInEditMode) 6 else 2.dp

    /** 高度扩大倍速, 用于留空区域显示动画 */
    private val heightMultiple = 5

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        // ViewGroup默认不执行onDraw，我们需要绘制
        setWillNotDraw(false)

        paint.color = MyColor.GREEN
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth.toFloat()
    }

    /**
     * 测量
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 无子View时分配个占位
        if (childCount == 0) {
            setMeasuredDimension(100, 100)
            return
        }

        // 遍历测量全部子View
        measureChildren(widthMeasureSpec, heightMeasureSpec)

        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)

        // 当宽为wrap_content时，计算全部子View宽度之和
        val width = if (widthSpecMode == MeasureSpec.AT_MOST) {
            var sum = 0
            children.forEach { child ->
                child as VisualElement
                // 指针元素绘制在下方，不参与宽度计算
                if (child.isPoint()) {
                    return@forEach
                }
                sum += child.measuredWidth + elementPadding
            }
            sum -= elementPadding
            // 防止宽度超过父ViewGroup限制
            resolveSize(sum, widthMeasureSpec)
        } else {
            // 否则使用超类方法
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        } + 2 * edgePadding

        // 当高为wrap_content时，寻找子View中的最大高度
        val height = if (heightSpecMode == MeasureSpec.AT_MOST) {
            var sum = 0
            children.forEach { child ->
                child as VisualElement
                if (child.isPoint()) {
                    return@forEach
                }
                if (child.measuredHeight > sum) {
                    sum = child.measuredHeight
                }
            }
            sum *= heightMultiple // 额外加大指定倍数用于动画
            // 防止高度超过父ViewGroup限制
            resolveSize(sum, heightMeasureSpec)
        } else {
            // 否则使用父方法
            getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        } + 2 * edgePadding

        // 设置最终宽高，完成测量
        setMeasuredDimension(width, height)
    }

    /**
     * 布局，形参四个参数是父ViewGroup参考系的位置
     *
     * 给子View布局位置
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var left = paddingLeft + edgePadding
        var pointLeft = paddingLeft + edgePadding

        children.forEach { child ->
            child as VisualElement
            val cw = child.measuredWidth
            val ch = child.measuredHeight

            // 在最下方绘制指针元素
            if (child.isPoint()) {
                val cl = pointLeft
                val cr = cl + cw
                val ct = height - edgePadding - child.measuredHeight
                val cb = ct + ch
                child.layout(cl, ct, cr, cb)
                pointLeft = cr + elementPadding
                return@forEach
            }

            // 绘制子元素
            val cl = left
            val cr = cl + cw
            val ct = height - 2 * edgePadding - elementPadding - 2 * child.measuredHeight
            val cb = ct + ch
            child.layout(cl, ct, cr, cb)
            left = cr + elementPadding
        }
    }

    /**
     * 绘制
     */
    override fun onDraw(canvas: Canvas) {
        // 画边框
        val l = paddingLeft.toFloat() + strokeWidth / 2f
        val r = width - paddingRight - strokeWidth / 2f
        val maxChildHeight = (height - 2f * edgePadding) / heightMultiple
        val t = (height - 3 * edgePadding - elementPadding - 2 * maxChildHeight) + strokeWidth / 2f
        val b = t + edgePadding + maxChildHeight + elementPadding
        canvas.drawRoundRect(l, t, r, b, corner, corner, paint)
    }
    // endregion

    // region 动画相关方法
    /**
     * 垂直向上动画
     * @param index Int ChildView下标
     * @param onLeft Boolean Child view下标是否相对在左边
     * @return Animator
     */
    fun up(index: Int, onLeft: Boolean = false): Animator {
        val child = get(index)
        val propertyName = "y"
        val childHeight = child.height.toFloat()
        val start = child.y
        val end = start - if (onLeft) {
            childHeight * (heightMultiple - 3) - elementPadding
        } else {
            childHeight * (heightMultiple - 2)
        }
        child.tag = Pair(start, end)
        return ObjectAnimator.ofFloat(child, propertyName, start, end)
    }

    /**
     * 垂直向下动画
     * @param index Int ChildView下标
     * @return Animator
     */
    fun down(index: Int): Animator {
        val child = getElementView(index)
        val propertyName = "y"
        val pair = child.tag as Pair<*, *>
        val start = pair.second as Float
        val end = pair.first as Float
        return ObjectAnimator.ofFloat(child, propertyName, start, end)
    }

    /**
     * 水平动画
     * @param index Int ChildView下标
     * @param movedCount Int 向左移动为正，向右移动为负
     * @return Animator
     */
    fun move(index: Int, movedCount: Int): Animator {
        val child = getElementView(index)
        val propertyName = "x"
        val distance = movedCount * (child.width + elementPadding)
        val start = child.x
        val end = start - distance
        return ObjectAnimator.ofFloat(child, propertyName, start, end)
    }

    /**
     * 选中动画，渐变为橙色
     * @param index Int ChildView下标
     * @return Animator
     */
    fun select(index: Int) : Animator {
        return gradient(index, MyColor.ORANGE)
    }

    /**
     * 取消选中动画，渐变回绿色
     * @param index Int ChildView下标
     * @return Animator
     */
    fun unselect(index: Int) : Animator {
        return gradient(index, MyColor.GREEN)
    }

    /**
     * 渐变动画
     * @param index Int ChildView下标
     * @param targetColor Int 指定颜色
     * @return Animator
     */
    private fun gradient(index: Int, @ColorInt targetColor: Int): Animator {
        val child = getElementView(index)
        return ObjectAnimator.ofInt(
            child, "color", child.color, targetColor
        ).apply {
            setEvaluator(ArgbEvaluator())
            duration = 500
        }
    }

    private fun getElementView(index: Int) = get(index) as VisualElement
    // endregion
}