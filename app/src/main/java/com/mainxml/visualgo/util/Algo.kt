package com.mainxml.visualgo.util

/**
 * 一参数回调
 */
typealias onOneIndex = (Int) -> Unit

/**
 * 两参数回调
 */
typealias onTwoIndex = (Int, Int) -> Unit

/**
 * 三参数回调
 */
typealias onThreeIndex = (Int, Int, Int) -> Unit

/**
 * 指针位置变动回调（指针名，指针新下标）
 */
typealias onPointChanged = (String, Int) -> Unit

/**
 * 算法
 *
 * 从小到大排序，修改比较符号可实现倒序。
 * @author zcp
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
    fun selectionSort(a: IntArray, onSwap: onTwoIndex, onPointChanged: onPointChanged) {
        val n = a.size
        // 外循环：未排序区间为[i, n - 1]
        for (i in 0..< n - 1) {
            var m = i
            onPointChanged("i", i)
            onPointChanged("m", m)
            // 内循环：寻找未排序区间的最小元素
            for (j in i + 1..< n) {
                //onPointChanged("j", j)
                if (a[j] < a[m]) {
                    m = j
                    onPointChanged("m", m)
                }
            }
            swap(a, i, m)
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
    fun bubbleSort(a: IntArray, onSwap: onTwoIndex) {
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
    fun insertionSort(a: IntArray, onUp: onTwoIndex, onMove: onThreeIndex, onDown: onOneIndex) {
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
     * 快速排序
     * ```
     * 一种基于分治策略的排序算法。选择数组中的某个元素作为基准数，
     * 将所有小于基准数的元素移动到其左侧，将所有大于基准数的元素移动到其右侧。
     *
     * 选取数组左端元素作为基准数，初始化两个指针 i 和 j 分别指向数组的两端。
     * 从小到大排序的话：
     * 1. 使用 j 从右向左寻找一个比基准数小的元素。
     * 2. 使用 i 从左向右寻找一个比基准数大的元素。
     * 3. 交换这两个位置的元素。
     * 4. 重复上述步骤。
     * 5. 当 i 和 j 重合时，这个位置的元素和基准数交换，并作为分割两个子数组的分界线。基准数已有序。
     * 6. 对两个子数组递归上述步骤。
     *
     * 如果基准数不在最左端则交换到最左端再继续。
     * 关于指针谁先开始问题：从基准数对向开始。
     *
     * 时间复杂度为 O(n log n)、自适应排序、非稳定排序。
     * 最差复杂度为 O(n^2)，没有归并排序稳定，但出现最差情况的概率很低。
     * ```
     * @param a IntArray
     * @param left Int
     * @param right Int
     * @param onSwap TwoIndexCallback
     */
    fun quickSort(a: IntArray, left: Int, right: Int, onSwap: onTwoIndex) {
        if (left >= right) {
            return
        }

        var i = left
        var j = right

        while (i < j) {
            // 从右往左寻找小于基准数的下标
            while (i < j && a[j] >= a[left]) {
                j--
            }
            // 从左往右寻找大于基准数的下标
            while (i < j && a[i] <= a[left]) {
                i++
            }
            // 交换
            swap(a, i, j)
            onSwap(i, j)
        }
        // 此时 i == j，和基准位置交换，基准数归位
        swap(a, left, i)
        onSwap(left, i)

        // 对基准数左子数组排序
        quickSort(a, left, i - 1, onSwap)
        // 对基准数右子数组排序
        quickSort(a, i + 1, right, onSwap)
    }

    /**
     * 归并排序
     * ```
     * &_&
     * ```
     * @param a IntArray
     * @param left Int
     * @param right Int
     */
    fun mergeSort(a: IntArray, left: Int, right: Int) {
        // 归 | 长度为 1 时终止
        if (left >= right) {
            return
        }

        // 递 | 递归二分切割
        val mid = (left + right) / 2
        mergeSort(a, left, mid)
        mergeSort(a, mid + 1, right)

        // 归 | 合并阶段（最先执行时长度为 2）

        // 准备一个临时数组来存储有序元素
        val sorted = IntArray(right - left + 1)

        // 在两个子数组中，将小的元素依次放入有序数组
        var i = left
        var j = mid + 1
        var k = 0
        while (i <= mid && j <= right) {
            sorted[k++] = if (a[i] < a[j]) a[i++] else a[j++]
        }
        // 将左子数组剩余元素放入有序数组
        while (i <= mid) {
            sorted[k++] = a[i++]
        }
        // 将右子数组剩余元素放入有序数组
        while (j <= right) {
            sorted[k++] = a[j++]
        }

        // 有序数组对应回原数组
        for (l in sorted.indices) {
            a[left + l] = sorted[l]
        }
    }

    private fun swap(a: IntArray, i: Int, j: Int) {
        val temp = a[i]
        a[i] = a[j]
        a[j] = temp
    }
}