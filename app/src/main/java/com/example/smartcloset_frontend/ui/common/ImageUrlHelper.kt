package com.example.smartcloset_frontend.ui.common

import com.example.smartcloset_frontend.BuildConfig
import com.example.smartcloset_frontend.network.ServerUrlHolder

/**
 * 画像URL生成を共通化するヘルパーオブジェクト
 */
object ImageUrlHelper {
    /**
     * 画像パスから完全なURLを生成する
     * @param imagePath 画像のパス（相対パスまたは完全なURL）
     * @return 完全なURL、またはnull（imagePathが空の場合）
     */
    fun buildImageUrl(imagePath: String?): String? {
        if (imagePath.isNullOrBlank()) return null
        
        return if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            imagePath
        } else {
            val baseUrl = (ServerUrlHolder.overrideBaseUrl ?: BuildConfig.SERVER_URL).trimEnd('/')
            val path = if (imagePath.startsWith("/")) imagePath else "/$imagePath"
            "$baseUrl$path"
        }
    }
    
    /**
     * リトライ用のURLを生成する
     * @param baseUrl ベースURL
     * @param retryKey リトライキー
     * @return リトライパラメータ付きURL
     */
    fun addRetryParameter(baseUrl: String, retryKey: Int): String {
        return if (retryKey > 0) {
            val separator = if (baseUrl.contains("?")) "&" else "?"
            "$baseUrl${separator}_retry=$retryKey"
        } else {
            baseUrl
        }
    }
    
    /**
     * 複数の画像パスからURLリストを生成する
     * @param imagePaths 画像パスのリスト
     * @return 完全なURLのリスト（重複なし）
     */
    fun buildImageUrls(imagePaths: List<String>): List<String> {
        return imagePaths
            .mapNotNull { buildImageUrl(it) }
            .distinct()
    }
}