package com.mainxml.visualgo.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
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

    /** 元素的值 */
    var value: Int = 0

    /** 元素颜色 */
    @ColorInt
    var color = MyColor.GREEN
        set(value) {
            field = value
            invalidate()
        }

    private val defaultSize: Int = if (isInEditMode) 72 else 24.dp
    private val textSize: Float = if (isInEditMode) 42f else 14.dp.toFloat()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()
    private val fontMetrics = paint.fontMetrics

    /**
     * 测量
     *
     * 只需为wrap_content时提供默认大小。
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 设置固定大小时可直接用setMeasuredDimension()，resolveSize()方法可以防止大小超出父类
        /* val measuredWidth = resolveSize(defaultSize, widthMeasureSpec)
        val measuredHeight = resolveSize(defaultSize, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight) */

        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)

        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        when {
            widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST -> {
                setMeasuredDimension(defaultSize, defaultSize)
            }
            widthSpecMode == MeasureSpec.AT_MOST -> {
                setMeasuredDimension(defaultSize, heightSpecSize)
            }
            heightSpecMode == MeasureSpec.AT_MOST -> {
                setMeasuredDimension(widthSpecSize, defaultSize)
            }
            else -> {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

    /**
     * 布局
     *
     * 此方法用于ViewGroup布局子View。当前是子View，直接留空。
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        /* no-op */
    }

    /**
     * 绘制
     *
     * 需要处理padding
     */
    override fun onDraw(canvas: Canvas) {
        // 绘制矩形
        val dw = width - paddingLeft - paddingRight
        val dh = height - paddingTop - paddingBottom
        rectF.set(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (paddingLeft + dw).toFloat(),
            (paddingTop + dh).toFloat()
        )
        paint.color = color
        canvas.drawRect(rectF, paint)

        // 绘制文字
        val text = value.toString()
        paint.textSize = textSize
        paint.color = MyColor.WHITE
        val textWidth = paint.measureText(text)
        val tx = rectF.width() / 2 - textWidth / 2
        val ty = rectF.centerY() - fontMetrics.top // fontMetrics.top是基线到顶部的距离，负数
        canvas.drawText(text, tx, ty, paint)
    }
}