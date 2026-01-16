package com.example.smartcloset_frontend.ui.generated_result.utils

import com.example.smartcloset_frontend.BuildConfig
import java.net.URLEncoder

/**
 * HTMLページのURLを生成するユーティリティ
 */
object UrlGenerator {
    /**
     * 画像URLからHTMLページのURLを生成する
     * @param imageUrl 画像のURL（相対パスまたは完全なURL）
     * @param coordinateId コーディネートID（オプション）
     * @return HTMLページの完全なURL
     */
    fun generateHtmlPageUrl(imageUrl: String, coordinateId: Int?): String {
        // ベースURLの前後のスペースと末尾のスラッシュを削除
        val baseUrl = BuildConfig.SERVER_URL.trim().trimEnd('/')

        if (baseUrl.isBlank()) {
            throw IllegalArgumentException("SERVER_URLが設定されていません")
        }

        // 画像URLを完全なURLに変換
        val fullImageUrl = if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            imageUrl.trim()
        } else {
            val imagePath = if (imageUrl.startsWith("/")) imageUrl else "/$imageUrl"
            "$baseUrl$imagePath"
        }

        // URLエンコード
        val encodedImageUrl = URLEncoder.encode(fullImageUrl, "UTF-8")

        // HTMLページのURLを生成
        // coordinateIdがある場合はそれを使用、ない場合はimage_urlパラメータを使用
        return if (coordinateId != null) {
            "$baseUrl/view_coordinate?coordinate_id=$coordinateId"
        } else {
            "$baseUrl/view_coordinate?image_url=$encodedImageUrl"
        }
    }
}