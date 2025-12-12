package com.example.smartcloset_frontend.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * シンプルなCookieJar実装（メモリ内のみ）
 * アプリ再起動時はCookieが失われます
 */
class SimpleCookieJar : CookieJar {
    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        cookieStore[host] = cookies.toMutableList()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        return cookieStore[host] ?: emptyList()
    }

    fun clearCookies() {
        cookieStore.clear()
    }
}

