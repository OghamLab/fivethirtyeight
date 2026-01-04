package com.ola.fivethirtyeight.preloaded

import android.content.Context
import android.webkit.WebView

object WebViewPreloadCache {
    private val cache = mutableMapOf<String, WebView>()

    fun getOrCreate(context: Context, url: String): WebView {
        return cache.getOrPut(url) {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                loadUrl(url)
            }
        }
    }
}