package com.mainxml.visualgo.util

/**
 * @author zcp
 */

/**
 * 交换两个位置的回调，左边参数要小于右边参数
 */
typealias onSwap = (Int, Int) -> Unit

/**
 * 算法
 */
object Algo {

    /**
     * 选择排序
     * ```
     * 从小到大排序，改变比较符号可以反过来。
     * 将输入列表分成【未排序】和【已排序】两个区，
     * 循环的从未排序区间再循环的选择最小的元素，将其放到已排序区间的末尾。
     *
     * 1. 初始状态下，所有元素未排序，即未排序区间为[0 .. n - 1]
     * 2. 选取区间[0 .. n - 1]中的最小元素，将其与索引 0 处的元素交换。完成后，数组前 1 个元素已排序。
     * 3. 选取区间[1 .. n - 1]中的最小元素，将其与索引 1 处的元素交换。完成后，数组前 2 个元素已排序。
     * 4. 以此类推。经过 n - 1 轮选择与交换后，数组前个 n - 1 个元素已排序。
     * 5. 仅剩的一个元素必定是最大元素，无须排序，因此数组排序完成。
     *
     * 时间复杂度为 O(n^2)、非自适应排序、非稳定排序
     * ```
     * @param a IntArray
     * @param onSwap onSwap
     */
    fun selectionSort(a: IntArray, onSwap: onSwap) {
        val n = a.size
        // 外循环：未排序区间为[i, n - 1]。
        // 排序到仅剩时一个元素必定是最大元素，无须排序，因此循环次数可以少一次。i ∈ [0, n - 2]
        for (i in 0..< n - 1) {
            // 内循环：找到未排序区间内的最小元素
            var k = i
            for (j in i + 1..< n) {
                if (a[j] < a[k]) {
                    k = j
                }
            }
            if (i == k) {
                continue
            }
            val tmp = a[i]
            a[i] = a[k]
            a[k] = tmp

            onSwap(i, k)
        }
    }

    /**
     * 冒泡排序
     * ```
     * 从小到大排序，改变比较符号可以反过来。
     * 连续地比较与交换相邻元素，一轮结束时最右侧的元素最大，最多 n - 1 轮保证数组完成排序。
     * ```
     * @param a IntArray
     * @param onSwap onSwap
     */
    fun bubbleSort(a: IntArray, onSwap: onSwap) {
        // 循环 n - 1 次，因为每轮最右侧元素最大，所以递减 1 来对二级循环做右界
        for (i in a.lastIndex downTo 1) {
            for (j in 0 ..< i) {
                // 比较与交换相邻元素
                if (a[j] > a[j + 1]) {
                    val tmp = a[j]
                    a[j] = a[j + 1]
                    a[j + 1] = tmp

                    onSwap(j, j + 1)
                }
            }
        }
    }

}

