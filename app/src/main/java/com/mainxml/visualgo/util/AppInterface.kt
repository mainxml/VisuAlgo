package com.mainxml.visualgo.util

import android.webkit.JavascriptInterface
import com.mainxml.visualgo.animation.SortAnimator

/**
 * 提供给Javascript调用的原生方法集合对象
 * @author zcp
 */
class AppInterface(private val arrayAnimator: SortAnimator) {

    val name = "\$app"

    @JavascriptInterface
    fun onSwap(i: Int, j: Int) {
        arrayAnimator.swap(i, j)
    }

    @JavascriptInterface
    fun onUp(i: Int, isFloating: Boolean = false) {
        arrayAnimator.up(i, isFloating)
    }

    @JavascriptInterface
    fun onDown(i: Int) {
        arrayAnimator.down(i)
    }

    @JavascriptInterface
    fun onMove(i: Int, j: Int, isFloating: Boolean = false) {
        arrayAnimator.move(i, j, isFloating)
    }

    @JavascriptInterface
    fun onPointMove(pointName: String, i: Int) {
        arrayAnimator.pointMove(pointName, i)
    }

    @JavascriptInterface
    fun onTrack(lineNumber: Int) {
        arrayAnimator.track(lineNumber)
    }

    @JavascriptInterface
    fun play() {
        arrayAnimator.play()
    }
}