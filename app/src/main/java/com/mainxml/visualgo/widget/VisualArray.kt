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
    /** 高度扩大倍速, 用于留空区域显示动画 */
    private val heightMultiple = 8
    /** 子View最高值 */
    private var maxChildHeight = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokeWidth = if (isInEditMode) 6 else 2.dp
    private val corner = 0f

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
        if (childCount == 0) {
            // 无子View时提供预览
            if (isInEditMode) {
                repeat(5) {
                    @Suppress("DrawAllocation")
                    addView(VisualElement(context).apply { value = it })
                }
            } else {
                setMeasuredDimension(0, 0)
            }
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
                if (child.type != VisualElement.Type.Element) {
                    return@forEach
                }
                sum += child.measuredWidth + elementPadding
            }
            if (sum > 0) {
                sum -= elementPadding
            }
            // 防止宽度超过父ViewGroup限制
            resolveSize(sum, widthMeasureSpec)
        } else {
            // 否则使用超类方法
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        } + 2 * edgePadding

        // 获取子View最高值
        var sum = 0
        children.forEach { child ->
            child as VisualElement
            if (child.type != VisualElement.Type.Element) {
                return@forEach
            }
            if (child.measuredHeight > sum) {
                sum = child.measuredHeight
            }
        }
        maxChildHeight = sum

        // 当高为wrap_content时，寻找子View中的最大高度
        val height = if (heightSpecMode == MeasureSpec.AT_MOST) {
            // 额外加高指定倍数用于动画
            sum *= heightMultiple
            // 防止高度超过父ViewGroup限制
            resolveSize(sum, heightMeasureSpec)
        } else {
            // 否则使用父方法
            getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        } + 5 * edgePadding

        // 设置最终宽高，完成测量
        setMeasuredDimension(width, height)
    }

    /**
     * 给子View布局位置
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var elementLeft = edgePadding
        var indexLeft = elementLeft
        val pointLeft = elementLeft

        val ch = maxChildHeight
        val t1 = height - edgePadding - ch
        val t2 = t1 - edgePadding - ch
        val t3 = t2 - edgePadding - ch - edgePadding / 2
        val t4 = t3 - edgePadding - ch

        var pointCount = 0

        children.forEach { child ->
            child as VisualElement
            val cw = child.measuredWidth
            when (child.type) {
                VisualElement.Type.Element -> {
                    // 在第四层高度绘制子元素
                    val cl = elementLeft
                    val cr = cl + cw
                    val cb = t4 + ch
                    child.layout(cl, t4, cr, cb)
                    elementLeft = cr + elementPadding
                }
                VisualElement.Type.Index -> {
                    // 在第三层高度绘制指针元素
                    val cl = indexLeft
                    val cr = cl + cw
                    val cb = t3 + ch
                    child.layout(cl, t3, cr, cb)
                    indexLeft = cr + elementPadding
                }
                VisualElement.Type.Point -> {
                    // 在第一第二层高度绘制指针元素
                    val ct = if (++pointCount > 2) t1 else t2
                    val cr = pointLeft + cw
                    val cb = ct + ch
                    child.layout(pointLeft, ct, cr, cb)
                }
                VisualElement.Type.TreeNode -> {
                    throw IllegalArgumentException("不支持树节点类型")
                }
            }
        }
    }

    /**
     * 绘制边框
     */
    override fun onDraw(canvas: Canvas) {
        val ch = maxChildHeight.toFloat()
        val t1 = height - edgePadding - ch
        val t2 = t1 - edgePadding - ch
        val t3 = t2 - edgePadding - ch - edgePadding / 2
        val t4 = t3 - edgePadding - ch

        val l = strokeWidth / 2f
        val t = t4 - edgePadding
        val r = width - strokeWidth / 2f
        val b = t4 + edgePadding + ch + edgePadding + ch - strokeWidth / 2
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
            childHeight * 3
        } else {
            childHeight * 2 - 2 * elementPadding
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