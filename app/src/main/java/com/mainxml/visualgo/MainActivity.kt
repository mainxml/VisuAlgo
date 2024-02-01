package com.mainxml.visualgo

import android.os.Bundle
import com.drake.brv.utils.setup
import com.mainxml.visualgo.animation.VisualArrayAnimator
import com.mainxml.visualgo.base.BaseActivity
import com.mainxml.visualgo.databinding.ActivityMainBinding
import com.mainxml.visualgo.databinding.ItemCodeLineBinding

/**
 * MainActivity
 * @author zcp
 */
class MainActivity : BaseActivity<MainVM, ActivityMainBinding>() {

    override val layoutId: Int
        get() = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
    }

    private fun initViews() {
        // 待排序数组
        val a = intArrayOf(5, 4, 3, 2, 1)

        // 动画师
        val animator = VisualArrayAnimator(binging.array)
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
        // 快速排序
        binging.quickSort.setOnClickListener {
            animator.quickSort(a)
        }
    }

    private fun setupSourceCode() {
        binging.rv.setup {
            addType<String>(R.layout.item_code_line)
            onBind {
                val bd = getBinding<ItemCodeLineBinding>()
                val code = getModel<String>()
                bd.tv.text = code
            }
            val codes = mutableListOf<String>()
            models = codes
        }
    }
}