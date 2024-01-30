package com.mainxml.visualgo.animation

import android.animation.Animator
import android.animation.AnimatorSet
import com.mainxml.visualgo.util.Algo
import com.mainxml.visualgo.util.OneIndexCallback
import com.mainxml.visualgo.util.ThreeIndexCallback
import com.mainxml.visualgo.util.TwoIndexCallback
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
        Algo.selectionSort(sortedArray, onSwap)
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

    /** 动画队列 */
    private val animatorQueue: Queue<LazyAnimator> = LinkedList()

    /** 播放中的动画 */
    private var playingAnimator: Animator? = null

    /**
     * 算法下标对子View实际下标的映射。
     *
     * 因为动画移动子View时并没有实际改变子View在ViewGroup中的index，
     * 所以对子View进行动画时需要映射到其实际下标。
     */
    private val viewIndexMap = mutableMapOf<Int, Int>()

    /** 原数组 */
    private lateinit var originArray: IntArray

    /** 已排序数组 */
    private lateinit var sortedArray: IntArray

    /**
     * 初始化数组和动画
     * @param a IntArray
     */
    private fun initSort(a: IntArray) {
        playingAnimator?.pause()
        playingAnimator?.removeAllListeners()
        animatorQueue.clear()
        visualArray.removeAllViews()

        originArray = a
        sortedArray = a.clone()
        viewIndexMap.clear()

        val c = visualArray.context

        // 创建对应输入数组的元素
        a.forEachIndexed { index, value ->
            visualArray.addView(VisualElement.create(c, value))
            // 初始化算法下标对子View实际下标的映射
            viewIndexMap[index] = index
        }
        // 添加下标元素，不参加排序
        a.indices.forEach {
            visualArray.addView(VisualElement.createIndex(c, it))
        }
        // 添加下标位置指针元素，不参加排序
        visualArray.addView(VisualElement.createPoint(c, "i"))
        visualArray.addView(VisualElement.createPoint(c, "j"))
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
                lazyAnimator().apply {
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

    /** 算法对元素交换的回调 */
    private val onSwap: TwoIndexCallback = { i, j ->
        swap(i, j)
    }

    /**
     * 动画交换两个元素
     * @param i Int
     * @param j Int
     */
    private fun swap(i: Int, j: Int) {
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
            return
        }

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

    /** 停留在空中的View的位置 */
    private var stayViewIndex = -1

    /** 算法对元素抬起的回调 */
    private val onUp: TwoIndexCallback = { i, stay ->
        up(i, stay == 1)
    }

    /**
     * 升起一个元素
     * @param i Int
     * @param isStay Boolean 升起后是否在空中停留
     */
    private fun up(i: Int, isStay: Boolean = false) {
        val vi = getViewIndex(i)
        if (isStay) {
            stayViewIndex = vi
        }
        val create: LazyAnimator = {
            visualArray.up(vi)
        }
        animatorQueue.offer(create)
    }

    /** 算法对元素下降的回调 */
    private val onDown: OneIndexCallback = { i ->
        down(i)
    }

    /**
     * 下降一个元素
     * @param i Int
     */
    private fun down(i: Int) {
        val vi: Int
        if (stayViewIndex != -1) {
            vi = stayViewIndex
            stayViewIndex = -1
        } else {
            vi = getViewIndex(i)
        }
        val create: LazyAnimator = {
            visualArray.down(vi)
        }
        animatorQueue.offer(create)
    }

    /** 算法对元素移动的回调 */
    private val onMove: ThreeIndexCallback = { i, j, b ->
        move(i, j, b == 1)
    }

    /**
     * 移动一个位置的元素到另一个位置
     * @param i Int
     * @param j Int
     * @param isStay Boolean 是否移动在空中停留的那个元素
     */
    private fun move(i: Int, j: Int, isStay: Boolean) {
        val vi = if (isStay) {
            stayViewIndex
        } else {
            getViewIndex(i)
        }
        val create: LazyAnimator = {
            visualArray.move(vi, i - j)
        }
        animatorQueue.offer(create)

        viewIndexMap[j] = vi
    }

    private fun playTogether(vararg animators: Animator): Animator {
        return AnimatorSet().apply {
            playTogether(*animators)
        }
    }

    private fun getViewIndex(i: Int) = viewIndexMap[i] as Int
}