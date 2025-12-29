package com.example.smartcloset_frontend.data

import kotlinx.serialization.Serializable

@Serializable
data class GenerateImageRequest(
    val image_paths: List<String>,
    val model_image_base64: String? = null,  // 人物画像のbase64データ（カメラ撮影時）
    val model_template: String? = null,  // テンプレート識別文字列（"mannequin" または "profile"）
    val coordinate_id: Int? = null
)
