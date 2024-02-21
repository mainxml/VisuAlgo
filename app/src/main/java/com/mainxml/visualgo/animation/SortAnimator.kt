package com.mainxml.visualgo.animation

import android.animation.Animator
import android.animation.AnimatorSet
import android.webkit.WebView
import androidx.core.animation.addListener
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

    /**
     * 算法下标对元素视图下标的映射。
     * 因为动画移动子View时并没有实际改变子View在ViewGroup中的index，
     * 所以对子View进行动画时需要映射到其实际下标。
     */
    private val viewIndexMap = mutableMapOf<Int, Int>()

    /** 指针视图的实际下标表 */
    private val pointViewIndexMap = mutableMapOf<String, Int>()

    /**
     * 初始化数组和动画
     * @param a IntArray
     */
    private fun initSort(a: IntArray) {
        playingAnimator?.pause()
        playingAnimator?.removeAllListeners()
        animatorQueue.clear()

        viewIndexMap.clear()
        pointViewIndexMap.clear()
        visualArray.removeAllViews()
        stepList.clear()

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
                animation.removeAllListeners()
                if (index > animatorQueue.lastIndex) {
                    return
                }
                if (endIndex in 1..<index) {
                    return
                }
                val self = this
                val lazyAnimator = animatorQueue[index++]
                lazyAnimator.invoke().apply {
                    playingAnimator = this
                    addListener(self)
                    start()
                }
            }
        }
        visualArray.post {
            val lazyAnimator = animatorQueue[index++]
            lazyAnimator().apply {
                playingAnimator = this
                addListener(listener)
                start()
            }
        }
    }

    /** 当前动画步骤 */
    private var curStep = 0

    /** 所有动画步骤列表 */
    private val stepList = mutableListOf<Step>()

    /**
     * 步骤记录
     * @param animationIndex Int 当前动画索引
     * @param elementIndexes IntArray 元素视图下标
     */
    private fun stepRecord(animationIndex: Int, vararg elementIndexes: Int) {
        val step = Step(animationIndex)
        val elementViewPosList = mutableListOf<Triple<Int, Float, Float>>()
        elementIndexes.forEach {
            val v = visualArray[it]
            if (v is VisualElement) {
                elementViewPosList.add(Triple(it, v.x, v.y))
            }
        }
        step.elementViewPosList = elementViewPosList
        stepList.add(step)
        curStep = stepList.lastIndex
    }

    /**
     * 上一步
     */
    fun previousStep() {
        if (curStep < 0) {
            return
        }
        if (curStep > stepList.lastIndex) {
            curStep = stepList.lastIndex
        }
        curStep--
        val step = stepList[curStep]
        step.elementViewPosList.forEach {
            visualArray[it.first].x = it.second
            visualArray[it.first].y = it.third
        }

        // 执行一次动画让代码高亮
        val start = if (curStep == 0) {
            1
        } else {
            findNextAnimationIndex(stepList[curStep - 1].animationIndex) + 1
        }
        play(start, start)
    }

    /**
     * 下一步
     */
    fun nextStep() {
        if (curStep > stepList.lastIndex) {
            return
        }
        val step = stepList[curStep]
        val start = if (curStep == 0) {
            1
        } else {
            findNextAnimationIndex(stepList[curStep - 1].animationIndex) + 1
        }
        val end = if (curStep != stepList.lastIndex) {
            findNextAnimationIndex(step.animationIndex)
        } else {
            animatorQueue.lastIndex
        }
        curStep++
        play(start, end)
    }

    private fun findNextAnimationIndex(searchStart: Int): Int {
        var v = 0
        for (i in searchStart .. animatorQueue.lastIndex) {
            val anim = animatorQueue[i].invoke()
            if (anim is AnimatorSet && anim.startDelay == 200L) {
                v = i - 1
                break
            }
        }
        return v
    }

    /**
     * 添加指针元素视图
     * @param names Array<out String>
     */
    private fun addPoints(vararg names: String) {
        val context = visualArray.context
        names.forEach { name ->
            visualArray.addView(VisualElement.createPoint(context, name))
            pointViewIndexMap[name] = visualArray.childCount - 1
        }
    }

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

    // region 动画相关成员
    /**
     * 两个元素交换
     * - i Int 元素下标1
     * - j Int 元素下标2
     */
    fun swap(i: Int, j: Int) {
        // 实际View的下标
        val vi = getViewIndex(i)
        val vj = getViewIndex(j)

        stepRecord(animatorQueue.lastIndex + 1, vi, vj)

        // 位置相等时只做渐变动画
        if (i == j) {
            visualArray.apply {
                listOf(
                    { select(vi) },
                    { unselect(vi) }
                ).forEach { creator ->
                    animatorQueue.offer(creator)
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
                    animatorQueue.offer(creator)
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

        stepRecord(animatorQueue.lastIndex + 1, vi)

        val lazyAnimator: LazyAnimator = {
            visualArray.up(vi)
        }
        animatorQueue.offer(lazyAnimator)
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

        stepRecord(animatorQueue.lastIndex + 1, vi)

        val lazyAnimator: LazyAnimator = {
            visualArray.down(vi)
        }
        animatorQueue.offer(lazyAnimator)
    }

    /**
     * 元素位置移动
     * - i Int 元素下标1
     * - j Int 元素下标2
     * - isFloating Int 是否移动在空中停留的那个元素
     */
    fun move(i: Int, j: Int, isFloating: Boolean = false) {
        val vi = if (isFloating) floatingViewIndex else getViewIndex(i)

        stepRecord(animatorQueue.lastIndex + 1, vi)

        val lazyAnimator: LazyAnimator = {
            visualArray.move(vi, i - j)
        }
        animatorQueue.offer(lazyAnimator)
        viewIndexMap[j] = vi
    }

    /**
     * 指针元素位置移动
     * - pointName 指针名
     * - i 指针新下标
     */
    fun pointMove(pointName: String, i: Int) {
        val targetPointIndex = pointViewIndexMap[pointName] ?: return
        val targetPoint = visualArray[targetPointIndex] as VisualElement
        val lazyAnimator: LazyAnimator = {
            val moveCount = targetPoint.value - i
            targetPoint.value = i
            visualArray.move(targetPointIndex, moveCount)
        }
        animatorQueue.offer(lazyAnimator)
    }

    /**
     * 跟踪代码行
     */
    fun track(lineNumber: Int) {
        val lazyAnimator: LazyAnimator = {
            AnimatorSet().apply {
                startDelay = 200
                addListener(onEnd = { _ ->
                    callJsHighlightLineNumber(lineNumber)
                })
            }
        }
        animatorQueue.offer(lazyAnimator)
    }

    private fun playTogether(vararg animators: Animator): Animator {
        return AnimatorSet().apply {
            playTogether(*animators)
        }
    }

    private fun getViewIndex(i: Int) = viewIndexMap[i] as Int
    // end region
}