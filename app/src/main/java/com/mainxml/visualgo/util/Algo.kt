package com.mainxml.visualgo.util

/**
 * @author zcp
 */

/**
 * 一个位置变动的回调
 */
typealias OneIndexCallback = (Int) -> Unit

/**
 * 两个位置变动的回调，左边参要小于右边参数
 */
typealias TwoIndexCallback = (Int, Int) -> Unit

/**
 * 算法
 *
 * 从小到大排序，修改比较符号可实现倒序。
 */
object Algo {

    /**
     * 选择排序
     * ```
     * 循环连续地从未排序区间选择最小的元素，将其与未排序区间开头元素交换并成为已排序区间的末尾，
     * 最多 n - 1 轮完成排序。
     *
     * 时间复杂度为 O(n^2)、非自适应排序、非稳定排序
     * ```
     * @param a IntArray
     * @param onSwap TwoIndexCallback
     */
    fun selectionSort(a: IntArray, onSwap: TwoIndexCallback) {
        val n = a.size
        // 初始已排序区间为空，未排序区间为[0, n - 1]
        // 外循环：未排序区间为[i, n - 1]
        for (i in 0..< n - 1) {
            var m = i
            // 内循环：寻找未排序区间的最小元素
            for (j in i + 1..< n) {
                if (a[j] < a[m]) {
                    m = j
                }
            }
            if (i == m) {
                continue
            }
            val tmp = a[i]
            a[i] = a[m]
            a[m] = tmp

            onSwap(i, m)
        }
    }

    /**
     * 冒泡排序
     * ```
     * 循环连续地比较相邻元素，符合条件就交换，每轮结束时最右侧的元素最大，
     * 最多 n - 1 轮完成排序。
     *
     * 时间复杂度为 O(n^2)、自适应排序、稳定排序
     * ```
     * @param a IntArray
     * @param onSwap TwoIndexCallback
     */
    fun bubbleSort(a: IntArray, onSwap: TwoIndexCallback) {
        val n = a.size
        // 外循环：未排序区间为[0, n - 1]
        // 因为每轮结束时最右侧元素最大，所以倒序递减来对内循环做右界
        for (i in n - 1 downTo 1) {
            // 内循环：比较相邻元素，符合条件就交换
            for (j in 0 ..< i) {
                if (a[j] > a[j + 1]) {
                    val tmp = a[j]
                    a[j] = a[j + 1]
                    a[j + 1] = tmp

                    onSwap(j, j + 1)
                }
            }
        }
    }

    /**
     * 插入排序
     * ```
     * 工作原理与手动整理一副牌的过程相似，
     * 在未排序区间选择一个基准元素，将该元素与其左侧已排序区间的元素逐一比较大小，并将该元素插入到正确的位置。
     *
     * 时间复杂度为 O(n^2)、自适应排序、稳定排序
     * ```
     * @param a IntArray
     * @param onUp OneIndexCallback
     * @param onMove TwoIndexCallback
     * @param onDown OneIndexCallback
     */
    fun insertionSort(
        a: IntArray,
        onUp: OneIndexCallback,
        onMove: TwoIndexCallback,
        onDown: OneIndexCallback,
    ) {
        val n = a.size
        // 初始已排序区间为[0]，未排序区间为[1, n - 1]
        // 外循环：未排序区间为[i, n - 1]
        for (i in 1 ..< n) {
            val base = a[i]
            var j = i - 1
            onUp(i)

            // 内循环：将 base 插入到已排序部分的正确位置
            while (j >= 0 && a[j] > base) {
                onMove(j, j + 1)

                a[j + 1] = a[j]
                --j
            }
            a[j + 1] = base

            onMove(i, j + 1)
            onDown(i)
        }
    }
}

