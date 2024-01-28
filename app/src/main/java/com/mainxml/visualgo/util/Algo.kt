package com.mainxml.visualgo.util

import java.util.Collections.swap

/**
 * @author zcp
 */

/**
 * 一个位置变动的回调
 */
typealias OneIndexCallback = (Int) -> Unit

/**
 * 两个位置变动的回调
 */
typealias TwoIndexCallback = (Int, Int) -> Unit

typealias ThreeIndexCallback = (Int, Int, Int) -> Unit

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
            swap(a, i, m)
            onSwap(i, m)
        }
    }

    /**
     * 冒泡排序 TODO FLAG
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
                    swap(a, j, j + 1)
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
     * @param onUp TwoIndexCallback 第一个参数表示位置，第二个参数表示是否在空中停留
     * @param onMove ThreeIndexCallback 前两个参数表示位置，第三个参数表示是否移动在空中停留的那个元素
     * @param onDown OneIndexCallback
     */
    fun insertionSort(
        a: IntArray,
        onUp: TwoIndexCallback,
        onMove: ThreeIndexCallback,
        onDown: OneIndexCallback
    ) {
        val n = a.size
        // 初始已排序区间为[0]，未排序区间为[1, n - 1]
        // 外循环：未排序区间为[i, n - 1]
        for (i in 1 ..< n) {
            val base = a[i]
            var j = i - 1
            onUp(i, 1)

            // 内循环：将 base 插入到已排序部分的正确位置
            while (j >= 0 && a[j] > base) {
                onMove(j, j + 1, 0)

                a[j + 1] = a[j]
                --j
            }
            a[j + 1] = base

            onMove(i, j + 1, 1)
            onDown(i)
        }
    }

    /**
     * 快速排序 TODO 有问题
     * 关于方向问题：从基准对向开始
     */
    fun quickSort(a: IntArray, left: Int, right: Int, onSwap: TwoIndexCallback) {
        if (left >= right) {
            return
        }

        var i = left
        var j = right

        while (i < j) {
            // 从右往左寻找小于基准值的下标
            while (i < j && a[j] >= a[left]) {
                j--
            }
            // 从左往右寻找大于基准值的下标
            while (i < j && a[i] <= a[left]) {
                i++
            }
            // 交换
            swap(a, i, j)
            onSwap(i, j)
        }
        // 此时 i == j，和基准位置交换，基准值归位
        swap(a, left, i)
        onSwap(i, j)

        // 对基准值左子数组排序
        quickSort(a, left, i - 1, onSwap)
        // 对基准值右子数组排序
        quickSort(a, i + 1, right, onSwap)
    }

    private fun swap(a: IntArray, i: Int, j: Int) {
        val temp = a[i]
        a[i] = a[j]
        a[j] = temp
    }
}