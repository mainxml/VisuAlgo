package com.mainxml.visualgo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import com.mainxml.visualgo.animation.VisualArrayAnimator
import com.mainxml.visualgo.base.BaseActivity
import com.mainxml.visualgo.databinding.ActivityMainBinding
import com.mainxml.visualgo.util.AssetsWebViewClient
import com.mainxml.visualgo.util.JsCallNativeInterface

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
        initWebView()
    }

    private lateinit var animator: VisualArrayAnimator

    private lateinit var jsCallNativeInterface: JsCallNativeInterface

    private fun initViews() {
        // 待排序数组
        val a = intArrayOf(5, 4, 3, 2, 1)

        // 动画师
        animator = VisualArrayAnimator(binging.array)

        // 显示数组
        animator.showArray(a)

        // 点击重制
        binging.reset.setOnClickListener {
            animator.resetSort()
        }
        // 选择排序
        binging.selectionSort.setOnClickListener {
            //animator.selectionSort(a)
            animator.selectionSortWithWeb(a, binging.webView)
        }
        // 冒泡排序
        binging.bubbleSort.setOnClickListener {
            //animator.bubbleSort(a)
            animator.bubbleSortWithWeb(a, binging.webView)
        }
        // 插入排序
        binging.insertionSort.setOnClickListener {
            //animator.insertionSort(a)
            animator.insertionSortWithWeb(a, binging.webView)
        }
        // 快速排序
        binging.quickSort.setOnClickListener {
            //animator.quickSort(a)
            animator.quickSortWithWeb(a, binging.webView)
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
            jsCallNativeInterface = JsCallNativeInterface(animator)
            addJavascriptInterface(jsCallNativeInterface, jsCallNativeInterface.name)

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