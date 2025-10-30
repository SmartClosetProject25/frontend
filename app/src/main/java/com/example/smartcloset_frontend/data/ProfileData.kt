package com.example.smartcloset_frontend.data

import kotlinx.serialization.Serializable

// サーバーと通信するために使用するデータ構造を定義するクラス
// @Serializableアノテーションにより、KotlinのオブジェクトとJSON形式との間で自動的に変換される
@Serializable
data class ProfileData(
    val name: String,
    val gender: String,
    val height: Int,
    val weight: Int,
    val personalColor: String,
    val skeleton: String
)
