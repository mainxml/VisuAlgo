package com.mainxml.visualgo

import android.os.Bundle
import com.mainxml.visualgo.animation.VisualArrayAnimator
import com.mainxml.visualgo.base.BaseActivity
import com.mainxml.visualgo.databinding.ActivityMainBinding

/**
 * MainActivity
 * @author zcp
 */
class MainActivity : BaseActivity<MainVM, ActivityMainBinding>() {

    private val animator: VisualArrayAnimator by lazy {
        VisualArrayAnimator(binging.array)
    }

    override val layoutId: Int
        get() = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 待排序数组
        val a = intArrayOf(1, 1, 2, 4, 5, 3)
        // 显示数组
        animator.showArray(a)
        // 点击重制
        binging.reset.setOnClickListener {
            animator.resetSort()
        }
        // 选择排序
        binging.selectionSort.setOnClickListener {
            animator.selectionSort(a)
        }
        // 冒泡排序
        binging.bubbleSort.setOnClickListener {
            animator.bubbleSort(a)
        }
        // 插入排序
        binging.insertionSort.setOnClickListener {
            animator.insertionSort(a)
        }
    }
}