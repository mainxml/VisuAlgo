package com.mainxml.visualgo.util

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader

/**
 * WebView客户端，访问网站时会映射到内置的assets目录的同名文件
 * @author zcp
 */
class AssetsWebViewClient(context: Context) : WebViewClient() {
    private val assetLoader: WebViewAssetLoader = WebViewAssetLoader.Builder()
        .setDomain("mainxml.com")
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
        .build()

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(request.url)
    }
}