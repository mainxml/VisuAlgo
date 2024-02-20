package com.mainxml.visualgo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import com.mainxml.visualgo.animation.SortAnimator
import com.mainxml.visualgo.base.BaseActivity
import com.mainxml.visualgo.base.BaseViewModel
import com.mainxml.visualgo.databinding.ActivityMainBinding
import com.mainxml.visualgo.util.AppInterface
import com.mainxml.visualgo.util.AssetsWebViewClient

/**
 * MainActivity
 * @author zcp
 */
class MainActivity : BaseActivity<BaseViewModel, ActivityMainBinding>() {

    override fun getLayoutId() = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        initWebView()
    }

    private lateinit var animator: SortAnimator

    private lateinit var appInterface: AppInterface

    private fun initViews() {
        // 待排序数组
        val a = intArrayOf(5, 4, 3, 2, 1)

        // 动画师
        animator = SortAnimator(binging.array, binging.webView)
        // 显示数组
        animator.showArray(a)

        // 点击重置
        binging.reset.setOnClickListener {
            animator.resetSort()
        }
        // 点击上一步
        binging.previousStep.setOnClickListener {
            animator.previousStep()
        }
        // 点击下一步
        binging.nextStep.setOnClickListener {
            animator.nextStep()
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

    private fun initWebView() {
        binging.webView.apply {
            // 开启js
            @SuppressLint("SetJavaScriptEnabled")
            settings.javaScriptEnabled = true

            // 设置访问内置assets目录的网页
            webViewClient = AssetsWebViewClient(context)

            // 设置多个原生方法映射到一个js对象内，供js调用
            appInterface = AppInterface(animator)
            addJavascriptInterface(appInterface, appInterface.name)

            // 加载内置assets目录的网页
            loadUrl("https://mainxml.com/assets/algo/index.html")
        }
    }

    override fun onResume() {
        super.onResume()
        binging.webView.onResume()
        binging.webView.resumeTimers()
    }

    override fun onPause() {
        super.onPause()
        binging.webView.onPause()
        binging.webView.pauseTimers()
    }

    override fun onDestroy() {
        super.onDestroy()
        binging.webView.stopLoading()
        (binging.webView.parent as ViewGroup).removeView(binging.webView)
        binging.webView.destroy()
    }
}