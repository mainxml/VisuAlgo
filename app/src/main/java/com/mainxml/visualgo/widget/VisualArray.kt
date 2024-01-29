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
    /** 边框圆角值 */
    private val corner = 0f
    /** 元素Padding */
    private val elementPadding = if (isInEditMode) 6 else 2.dp
    /** 画笔线宽 */
    private val strokeWidth = if (isInEditMode) 6f else 2.dp.toFloat()

    /** 高度扩大倍速, 用于留空区域显示动画 */
    private val heightMultiple = 4

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        // ViewGroup默认不执行onDraw，我们需要绘制
        setWillNotDraw(false)

        paint.color = MyColor.GREEN
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
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

        // 根据子View决定自身宽高
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)

        val resolveWidth = if (widthSpecMode == MeasureSpec.AT_MOST) {
            // 当宽为wrap_content时，计算全部子View宽度之和
            var widthSum = 0
            children.forEach { child ->
                widthSum += child.measuredWidth + elementPadding
            }
            widthSum -= elementPadding
            // 防止宽度超过父ViewGroup限制
            resolveSize(widthSum, widthMeasureSpec)
        } else {
            // 否则使用父方法
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        } + 2 * edgePadding

        val resolveHeight = if (heightSpecMode == MeasureSpec.AT_MOST) {
            // 当高为wrap_content时，寻找子View中的最大高度
            var heightSum = 0
            children.forEach { child ->
                if (child.measuredHeight > heightSum) {
                    heightSum = child.measuredHeight
                }
            }
            heightSum *= heightMultiple // 额外加大五倍用于动画
            // 防止高度超过父ViewGroup限制
            resolveSize(heightSum, heightMeasureSpec)
        } else {
            // 否则使用父方法
            getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        } + 2 * edgePadding

        // 设置最终宽高，完成测量
        setMeasuredDimension(resolveWidth, resolveHeight)
    }

    /**
     * 布局，形参四个参数是父ViewGroup参考系的位置
     *
     * 给子View布局位置
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var left = 0
        children.forEach { child ->
            val cw = child.measuredWidth
            val ch = child.measuredHeight
            val cl = edgePadding + left
            val ct = height - edgePadding - (height - 2 * edgePadding) / heightMultiple
            val cr = cl + cw
            val cb = ct + ch
            child.layout(cl, ct, cr, cb)
            left += cw + elementPadding
        }
    }

    /**
     * 绘制
     */
    override fun onDraw(canvas: Canvas) {
        // 画边框
        val l = paddingLeft.toFloat() + strokeWidth / 2
        val t = height.toFloat() - edgePadding - edgePadding - get(0).height + strokeWidth / 2
        val r = (paddingLeft + width).toFloat() - strokeWidth / 2
        val b = (paddingTop + height).toFloat() - strokeWidth / 2
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
            childHeight * (heightMultiple - 2) - elementPadding
        } else {
            childHeight * (heightMultiple - 1)
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