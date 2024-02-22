package com.mainxml.visualgo.animation

import android.animation.Animator
import android.animation.AnimatorSet
import android.graphics.PointF
import android.webkit.WebView
import android.widget.Toast
import androidx.core.animation.addListener
import androidx.core.view.forEach
import androidx.core.view.get
import com.mainxml.visualgo.widget.VisualArray
import com.mainxml.visualgo.widget.VisualElement
import java.util.LinkedList

/**
 * 排序动画 管理和调度动画
 * @param visualArray VisualArray
 * @param webView WebView
 * @author zcp
 */
class SortAnimator(private val visualArray: VisualArray, private val webView: WebView) {

    /** 原数组 */
    private lateinit var originArray: IntArray
    /** 已排序数组 */
    private lateinit var sortedArray: IntArray

    /** 动画队列 */
    private val animatorQueue: LinkedList<LazyAnimator> = LinkedList()
    /** 播放中的动画 */
    private var playingAnimator: Animator? = null
    /** 是否首次播放 */
    private var isFirstPlay = true

    /** 当前动画跟踪步骤 */
    private var curTrackStep = 0
    /** 动画跟踪步骤列表 */
    private val trackStepList = mutableListOf<TrackStep>()

    /**
     * 算法下标对元素视图下标的映射
     * 因为动画移动子View时并没有实际改变子View在ViewGroup中的下标，
     * 所以对子View进行动画时需要映射到其实际下标。
     */
    private val viewIndexMap = mutableMapOf<Int, Int>()

    /** 指针视图的实际下标表 <指针名称, 指针视图下标> */
    private val pointViewIndexMap = mutableMapOf<String, Int>()

    /**
     * 初始化数组和动画
     * @param a IntArray
     */
    private fun initSort(a: IntArray) {
        playingAnimator?.pause()
        playingAnimator?.removeAllListeners()

        isFirstPlay = true
        animatorQueue.clear()
        viewIndexMap.clear()
        pointViewIndexMap.clear()
        visualArray.removeAllViews()
        trackStepList.clear()

        sortedArray = a
        originArray = a.clone()

        val context = visualArray.context

        // 创建对应输入数组的元素
        a.forEachIndexed { index, value ->
            visualArray.addView(VisualElement.create(context, value))
            // 初始化算法下标对子View实际下标的映射
            viewIndexMap[index] = index
        }
        // 添加下标元素，不参加排序
        a.indices.forEach {
            visualArray.addView(VisualElement.createIndex(context, it))
        }

        callJsHighlightLineNumber(1)
    }

    /**
     * 重设排序
     */
    fun resetSort() {
        if (::originArray.isInitialized) {
            initSort(originArray)
        }
    }

    /**
     * 显示一个静态数组
     * @param a IntArray
     */
    fun showArray(a: IntArray) {
        initSort(a)
    }

    /**
     * 选择排序
     * @param a IntArray
     */
    fun selectionSort(a: IntArray) {
        initSort(a)

        callJsShowSourceCode("selectionSort")
        callJsAnimateSort("selectionSort", a)
    }

    /**
     * 冒泡排序
     * @param a IntArray
     */
    fun bubbleSort(a: IntArray) {
        initSort(a)

        callJsShowSourceCode("bubbleSort")
        callJsAnimateSort("bubbleSort", a)
    }

    /**
     * 插入排序
     * @param a IntArray
     */
    fun insertionSort(a: IntArray) {
        initSort(a)

        callJsShowSourceCode("insertionSort")
        callJsAnimateSort("insertionSort", a)
    }

    /**
     * 快速排序
     * @param a IntArray
     */
    fun quickSort(a: IntArray) {
        initSort(a)

        callJsShowSourceCode("quickSort")
        callJsAnimateSort("quickSort", a)
    }

    /**
     * 播放动画
     */
    fun play(startIndex: Int = 0, endIndex: Int = 0) {
        var index = startIndex
        val listener = object : AnimatorListenerImp() {
            override fun onAnimationEnd(animation: Animator) {
                playingAnimator?.removeAllListeners()

                trackStepRecord(index)

                if (index > animatorQueue.lastIndex) {
                    isFirstPlay = false
                    playingAnimator = null
                    return
                }
                if (endIndex in 1..<index) {
                    playingAnimator = null
                    return
                }

                val self = this
                val lazyAnimator = animatorQueue[index++]
                lazyAnimator.creator().apply {
                    playingAnimator = this
                    addListener(self)
                    start()
                }
            }
        }

        trackStepRecord(index)
        val lazyAnimator = animatorQueue[index++]
        val animation = lazyAnimator.creator().apply {
            playingAnimator = this
            addListener(listener)
        }
        visualArray.post {
            animation.start()
        }
    }

    /**
     * 记录跟踪代码的步骤
     * @param animationIndex Int 当前动画索引
     */
    private fun trackStepRecord(animationIndex: Int) {
        if (!isFirstPlay) {
            return
        }
        if (animationIndex > animatorQueue.lastIndex) {
            return
        }
        val lazyAnimator = animatorQueue[animationIndex]
        if (lazyAnimator.codeLineNumber == null) {
            return
        }

        // 记录每步骤的视图位置数据
        val elementPoints = mutableListOf<PointF>()
        visualArray.forEach { element ->
            if (element is VisualElement) {
                val x = element.x
                var y = element.y
                if (element.type == VisualElement.Type.Point) {
                    y = element.value.toFloat() // 指针元素时改为记录指针值
                }
                elementPoints.add(PointF(x, y))
            }
        }
        trackStepList.add(TrackStep(lazyAnimator.codeLineNumber, animationIndex, elementPoints))
        curTrackStep = trackStepList.lastIndex
    }

    /**
     * 上一步
     */
    fun previousStep() {
        if (playingAnimator != null) {
            Toast.makeText(visualArray.context, "动画播放中", Toast.LENGTH_SHORT).show()
            return
        }
        if (curTrackStep - 1 < 0) {
            Toast.makeText(visualArray.context, "已经是第一步", Toast.LENGTH_SHORT).show()
            return
        }
        curTrackStep--
        val trackStep = trackStepList[curTrackStep]
        val elementPoints = trackStep.elementPoints
        elementPoints.forEachIndexed { index, point ->
            val element = visualArray[index]
            if (element is VisualElement) {
                element.x = point.x
                //element.y = point.y
                if (element.type == VisualElement.Type.Point) {
                    element.value = point.y.toInt()
                } else {
                    element.y = point.y
                }
            }
        }

        callJsHighlightLineNumber(trackStep.codeLineNumber)
    }

    /**
     * 下一步
     */
    fun nextStep() {
        if (playingAnimator != null) {
            Toast.makeText(visualArray.context, "动画播放中", Toast.LENGTH_SHORT).show()
            return
        }
        if (curTrackStep + 1 > trackStepList.lastIndex) {
            Toast.makeText(visualArray.context, "已经是最后一步", Toast.LENGTH_SHORT).show()
            return
        }
        val trackStep = trackStepList[curTrackStep]
        val start = trackStep.animationIndex
        val end = trackStepList[curTrackStep + 1].animationIndex
        curTrackStep++
        play(start, end)
    }

    /**
     * 两个元素交换
     * - i Int 元素下标1
     * - j Int 元素下标2
     */
    fun swap(i: Int, j: Int) {
        // 实际View的下标
        val vi = getViewIndex(i)
        val vj = getViewIndex(j)

        // 位置相等时只做渐变动画
        if (i == j) {
            visualArray.apply {
                listOf(
                    { select(vi) },
                    { unselect(vi) }
                ).forEach { creator ->
                    animatorQueue.offer(LazyAnimator(creator))
                }
            }
        } else {
            // 位置不等时合成交换动画
            visualArray.apply {
                listOf(
                    { playTogether(select(vi), select(vj)) },
                    { playTogether(up(vi, true), up(vj)) },
                    { playTogether(move(vi, i - j), move(vj, j - i)) },
                    { playTogether(down(vi), down(vj)) },
                    { playTogether(unselect(vi), unselect(vj)) }
                ).forEach { creator ->
                    animatorQueue.offer(LazyAnimator(creator))
                }
            }

            // 更新映射下标
            viewIndexMap[i] = vj
            viewIndexMap[j] = vi
        }
    }

    /** 停留在空中的元素的下标 */
    private var floatingViewIndex = -1

    /**
     * 元素升起
     * - i Int 元素下标
     * - isFloating Int 是否升起后停留在空中
     */
    fun up(i: Int, isFloating: Boolean = false) {
        val vi = getViewIndex(i)
        if (isFloating) {
            floatingViewIndex = vi
        }
        animatorQueue.offer(LazyAnimator({
            visualArray.up(vi)
        }))
    }

    /**
     * 元素下降
     * - i Int 元素下标
     */
    fun down(i: Int) {
        val vi: Int
        if (floatingViewIndex != -1) {
            vi = floatingViewIndex
            floatingViewIndex = -1
        } else {
            vi = getViewIndex(i)
        }
        animatorQueue.offer(LazyAnimator({
            visualArray.down(vi)
        }))
    }

    /**
     * 元素位置移动
     * - i Int 元素下标1
     * - j Int 元素下标2
     * - isFloating Int 是否移动在空中停留的那个元素
     */
    fun move(i: Int, j: Int, isFloating: Boolean = false) {
        val vi = if (isFloating) floatingViewIndex else getViewIndex(i)
        animatorQueue.offer(LazyAnimator({
            visualArray.move(vi, i - j)
        }))
        viewIndexMap[j] = vi
    }

    /**
     * 指针元素位置移动
     * - pointName 指针名
     * - i 指针新下标
     */
    fun pointMove(pointName: String, i: Int) {
        animatorQueue.offer(LazyAnimator({
            val pointViewIndex = pointViewIndexMap[pointName]
                ?: throw IllegalArgumentException("不存在$pointName")
            val pointView = visualArray[pointViewIndex] as VisualElement
            val moveCount = pointView.value - i
            pointView.value = i
            visualArray.move(pointViewIndex, moveCount)
        }))
    }

    /**
     * 跟踪代码行
     */
    fun track(codeLineNumber: Int) {
        animatorQueue.offer(LazyAnimator({
            AnimatorSet().apply {
                startDelay = 200
                addListener(onEnd = { _ ->
                    callJsHighlightLineNumber(codeLineNumber)
                })
            }
        }, codeLineNumber))
    }

    /**
     * 添加指针元素视图
     * @param names Array<out String>
     */
    fun addPoints(vararg names: String) {
        visualArray.post {
            names.forEach { name ->
                visualArray.addView(VisualElement.createPoint(visualArray.context, name))
                pointViewIndexMap[name] = visualArray.childCount - 1
            }
        }
    }

    private fun playTogether(vararg animators: Animator): Animator {
        return AnimatorSet().apply {
            playTogether(*animators)
        }
    }

    private fun getViewIndex(i: Int) = viewIndexMap[i] as Int

    /**
     * 调用js的显示源码函数
     * @param algo String 算法名称
     */
    private fun callJsShowSourceCode(algo: String) {
        val script = "javascript:showSourceCode('${algo}')"
        webView.evaluateJavascript(script, null)
    }

    /**
     * 调用js的动画排序函数
     * @param algo String 算法名称
     * @param a IntArray 数组
     */
    private fun callJsAnimateSort(algo: String, a: IntArray) {
        val script = "javascript:animateSort('${algo}', ${a.toList()})"
        webView.evaluateJavascript(script, null)
    }

    /**
     * 调用js的高亮代码行函数
     * @param lineNumber Int
     */
    private fun callJsHighlightLineNumber(lineNumber: Int) {
        val script = "javascript:highlightLineNumber('${lineNumber}')"
        webView.evaluateJavascript(script, null)
    }
}