package com.mainxml.visualgo.util

import android.webkit.JavascriptInterface
import com.mainxml.visualgo.animation.VisualArrayAnimator

/**
 * Javascript调用原生接口
 * @author zcp
 */
class JsCallNativeInterface(private val arrayAnimator: VisualArrayAnimator) {

    val name = "\$app"

    @JavascriptInterface
    fun onSwap(i: Int, j: Int) {
        arrayAnimator.onSwap(i, j)
    }

    @JavascriptInterface
    fun onUp(i: Int, isStay: Int = 0) {
        arrayAnimator.onUp(i, isStay)
    }

    @JavascriptInterface
    fun onDown(i: Int) {
        arrayAnimator.onDown(i)
    }

    @JavascriptInterface
    fun onMove(i: Int, j: Int, isStay: Int = 0) {
        arrayAnimator.onMove(i, j, isStay)
    }

    @JavascriptInterface
    fun onPointChanged(pointName: String, changedIndex: Int) {
        arrayAnimator.onPointChanged(pointName, changedIndex)
    }

    @JavascriptInterface
    fun play() {
        arrayAnimator.play()
    }
}