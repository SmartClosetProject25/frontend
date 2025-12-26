package com.example.smartcloset_frontend.data

import kotlinx.serialization.Serializable

@Serializable
data class ImageResponse(
    val status: String,
    val image: String? = null,  // 後方互換性のため残す
    val image_url: String? = null,  // 相対パス
    val image_url_full: String? = null,  // 完全なURL（優先的に使用）
    val message: String? = null
)
