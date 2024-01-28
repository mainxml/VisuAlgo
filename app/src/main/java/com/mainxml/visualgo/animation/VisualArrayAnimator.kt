package com.mainxml.visualgo.animation

import android.animation.Animator
import android.animation.AnimatorSet
import com.mainxml.visualgo.util.Algo
import com.mainxml.visualgo.util.TwoIndexCallback
import com.mainxml.visualgo.util.OneIndexCallback
import com.mainxml.visualgo.util.ThreeIndexCallback
import com.mainxml.visualgo.widget.VisualArray
import com.mainxml.visualgo.widget.VisualElement
import java.util.LinkedList
import java.util.Queue

/**
 * 组织管理和调度数组动画
 * @param viewGroup VisualArray
 * @author zcp
 */
class VisualArrayAnimator(private val viewGroup: VisualArray) {

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
        viewGroup.removeAllViews()

        originArray = a
        sortedArray = a.clone()
        viewIndexMap.clear()
        a.forEachIndexed { index, value ->
            val child = VisualElement(viewGroup.context).also { it.value = value }
            viewGroup.addView(child)
            viewIndexMap[index] = index
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
                animatorQueue.poll()?.apply {
                    create(indexes).apply {
                        playingAnimator = this
                        addListener(self)
                        start()
                    }
                }
            }
        }
        viewGroup.post {
            animatorQueue.poll()?.apply {
                create(indexes).apply {
                    playingAnimator = this
                    addListener(listener)
                    start()
                }
            }
        }
    }

    /** 算法对元素交换的回调 */
    private val onSwap: TwoIndexCallback = { i, j ->
        animatorQueue.offer(LazyAnimator(listOf(i, j), ::swap))
    }

    /**
     * 交换两个元素
     * @param indexes: List<Int>
     */
    private fun swap(indexes: List<Int>): Animator {
        if (indexes.size != 2) {
            throw IllegalArgumentException()
        }

        // 算法下标
        val i = indexes[0]
        val j = indexes[1]

        // 实际View的下标
        val vi = getViewIndex(i)
        val vj = getViewIndex(j)

        // 合成动画
        val animator: Animator
        viewGroup.apply {
            animator = playSequentially(
                playTogether(select(vi), select(vj)),
                playTogether(up(vi, true), up(vj, false)),
                playTogether(move(vi, i - j), move(vj, j - i)),
                playTogether(down(vi), down(vj))
            )
        }

        // 更新映射下标
        viewIndexMap[i] = vj
        viewIndexMap[j] = vi

        return animator
    }

    /** 停留在空中的View的位置 */
    private var stayViewIndex = -1

    /** 算法对元素抬起的回调 */
    private val onUp: TwoIndexCallback = { i, stay ->
        animatorQueue.offer(LazyAnimator(listOf(i, stay), ::up))
    }

    /**
     * 升起一个元素
     * @param indexes List<Int> 第一个参数表示位置，第二个参数表示是否在空中停留(0:false; 1:true)
     * @return Animator
     */
    private fun up(indexes: List<Int>): Animator {
        val i = indexes[0]
        val vi = getViewIndex(i)

        val isStay = indexes[1]
        if (isStay == 1) {
            stayViewIndex = vi
        }

        return viewGroup.up(vi, false)
    }

    /** 算法对元素下降的回调 */
    private val onDown: OneIndexCallback = { i ->
        animatorQueue.offer(LazyAnimator(listOf(i), ::down))
    }

    /**
     * 降下一个元素
     * @param indexes List<Int>
     * @return Animator
     */
    private fun down(indexes: List<Int>): Animator {
        val i = indexes[0]
        val vi: Int
        if (stayViewIndex != -1) {
            vi = stayViewIndex
            stayViewIndex = -1
        } else {
            vi = getViewIndex(i)
        }
        return viewGroup.down(vi)
    }

    /** 算法对元素移动的回调 */
    private val onMove: ThreeIndexCallback = { i, j, b ->
        animatorQueue.offer(LazyAnimator(listOf(i, j, b), ::move))
    }

    /**
     * 移动一个位置的元素到另一个位置
     * @param indexes List<Int> 前两个参数表示位置，第三个参数表示是否移动在空中停留的元素(0:false; 1:true)
     * @return Animator
     */
    private fun move(indexes: List<Int>): Animator {
        val i = indexes[0]
        val j = indexes[1]
        val isStay = indexes[2]
        val vi = if (isStay == 1) {
             stayViewIndex
        } else {
            getViewIndex(i)
        }
        val animator = viewGroup.move(vi, i - j)
        viewIndexMap[j] = vi
        return animator
    }

    private fun playSequentially(vararg animators: Animator): Animator {
        return AnimatorSet().apply {
            playSequentially(*animators)
        }
    }

    private fun playTogether(vararg animators: Animator): Animator {
        return AnimatorSet().apply {
            playTogether(*animators)
        }
    }

    private fun getViewIndex(i: Int) = viewIndexMap[i] as Int
}