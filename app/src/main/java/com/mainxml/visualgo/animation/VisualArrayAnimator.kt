package com.mainxml.visualgo.animation

import android.animation.Animator
import android.animation.AnimatorSet
import androidx.core.view.get
import com.mainxml.visualgo.util.Algo
import com.mainxml.visualgo.util.onOneIndex
import com.mainxml.visualgo.util.onPointChanged
import com.mainxml.visualgo.util.onThreeIndex
import com.mainxml.visualgo.util.onTwoIndex
import com.mainxml.visualgo.widget.VisualArray
import com.mainxml.visualgo.widget.VisualElement
import java.util.LinkedList
import java.util.Queue

/**
 * 组织管理和调度数组动画
 * @param visualArray VisualArray
 * @author zcp
 */
class VisualArrayAnimator(private val visualArray: VisualArray) {

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
        addPoints("i", "m")
        Algo.selectionSort(sortedArray, onSwap, onPointChanged)
        play()
    }

    /**
     * 冒泡排序
     * @param a IntArray
     */
    fun bubbleSort(a: IntArray) {
        initSort(a)
        Algo.bubbleSort(sortedArray, onSwap)
        play()
    }

    /**
     * 插入排序
     * @param a IntArray
     */
    fun insertionSort(a: IntArray) {
        initSort(a)
        Algo.insertionSort(sortedArray, onUp, onMove, onDown)
        play()
    }

    /**
     * 快速排序
     * @param a IntArray
     */
    fun quickSort(a: IntArray) {
        initSort(a)
        Algo.quickSort(sortedArray, 0, sortedArray.lastIndex, onSwap)
        play()
    }

    /**
     * 重设排序
     */
    fun resetSort() {
        if (::originArray.isInitialized) {
            initSort(originArray)
        }
    }

    /** 原数组 */
    private lateinit var originArray: IntArray

    /** 已排序数组 */
    private lateinit var sortedArray: IntArray

    /** 动画队列 */
    private val animatorQueue: Queue<LazyAnimator> = LinkedList()

    /** 播放中的动画 */
    private var playingAnimator: Animator? = null

    /**
     * 算法下标对元素视图下标的映射。
     *
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

        originArray = a
        sortedArray = a.clone()

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
     * 播放动画
     */
    private fun play() {
        val listener = object : AnimatorListenerImp() {
            override fun onAnimationEnd(animation: Animator) {
                animation.removeAllListeners()
                val self = this
                val lazyAnimator = animatorQueue.poll() ?: return
                lazyAnimator.invoke().apply {
                    playingAnimator = this
                    addListener(self)
                    start()
                }
            }
        }
        visualArray.post {
            val lazyAnimator = animatorQueue.poll() ?: return@post
            lazyAnimator().apply {
                playingAnimator = this
                addListener(listener)
                start()
            }
        }
    }

    /**
     * 两个元素交换，添加动画
     * - i Int 元素位置1
     * - j Int 元素位置2
     */
    private val onSwap: onTwoIndex = { i, j ->
        // 实际View的下标
        val vi = getViewIndex(i)
        val vj = getViewIndex(j)

        // 位置相等时只做渐变动画
        if (i == j) {
            visualArray.apply {
                listOf(
                    { playTogether(select(vi), select(vj)) },
                    { playTogether(unselect(vi), unselect(vj)) }
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

    /** 停留在空中的View的位置 */
    private var stayingViewIndex = -1

    /**
     * 元素升起，添加动画
     * - i Int 元素位置
     * - isStay Int 是否升起后停留在空中
     */
    private val onUp: onTwoIndex = { i, isStay ->
        val vi = getViewIndex(i)
        if (isStay == 1) {
            stayingViewIndex = vi
        }
        val create: LazyAnimator = {
            visualArray.up(vi)
        }
        animatorQueue.offer(create)
    }

    /**
     * 元素下降，添加动画
     * - i Int 元素位置
     */
    private val onDown: onOneIndex = { i ->
        val vi: Int
        if (stayingViewIndex != -1) {
            vi = stayingViewIndex
            stayingViewIndex = -1
        } else {
            vi = getViewIndex(i)
        }
        val create: LazyAnimator = {
            visualArray.down(vi)
        }
        animatorQueue.offer(create)
    }

    /**
     * 元素位置发生移动，添加动画
     * - i Int 元素位置1
     * - j Int 元素位置2
     * - isStay Int 是否移动在空中停留的那个元素
     */
    private val onMove: onThreeIndex = { i, j, isStay ->
        val vi = if (isStay == 1) {
            stayingViewIndex
        } else {
            getViewIndex(i)
        }
        val create: LazyAnimator = {
            visualArray.move(vi, i - j)
        }
        animatorQueue.offer(create)

        viewIndexMap[j] = vi
    }

    /**
     * 指针元素位置发生改变，添加动画
     * - pointName 指针名
     * - changedIndex 指针新位置
     */
    private val onPointChanged: onPointChanged = f@ { pointName, changedIndex ->
        val targetPointIndex = pointViewIndexMap[pointName] ?: return@f
        val targetPoint = visualArray[targetPointIndex] as VisualElement
        val create: LazyAnimator = {
            val moveCount = targetPoint.value - changedIndex
            targetPoint.value = changedIndex
            visualArray.move(targetPointIndex, moveCount)
        }
        animatorQueue.offer(create)
    }

    private fun playTogether(vararg animators: Animator): Animator {
        return AnimatorSet().apply {
            playTogether(*animators)
        }
    }

    private fun getViewIndex(i: Int) = viewIndexMap[i] as Int
}