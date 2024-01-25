package com.mainxml.visualgo

import android.os.Bundle
import com.mainxml.visualgo.animation.VisualAnimator
import com.mainxml.visualgo.base.BaseActivity
import com.mainxml.visualgo.databinding.ActivityMainBinding

/**
 * MainActivity
 * @author zcp
 */
class MainActivity : BaseActivity<MainVM, ActivityMainBinding>() {

    private lateinit var visualAnimator: VisualAnimator

    override val layoutId: Int
        get() = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 待排序数组
        val a = intArrayOf(4, 5, 3, 2, 1)

        // 创建动画管理者
        visualAnimator = VisualAnimator(binging.array)

        // 显示数组
        visualAnimator.showArray(a)

        // 点击重制
        binging.reset.setOnClickListener {
            visualAnimator.resetSort()
        }

        // 选择排序
        binging.selectionSort.setOnClickListener {
            visualAnimator.selectionSort(a)
        }

        // 冒泡排序
        binging.bubbleSort.setOnClickListener {
            visualAnimator.bubbleSort(a)
        }
    }
}