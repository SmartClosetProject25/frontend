package com.example.smartcloset_frontend.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// サーバーと通信するために使用するデータ構造を定義するクラス
// @Serializableアノテーションにより、KotlinのオブジェクトとJSON形式との間で自動的に変換される
@Serializable
data class ProfileData(
    val userId: Int?,
    val name: String,
    val gender: String,
    val height: Int,
    val weight: Int,
    val personalColor: String,
    val skeleton: String
)
@Serializable
data class GetProfileResponse(
    val ok: Boolean,
    @SerialName("user_id") val userId: Int,
    val profile: ProfileDto,
    val counts: CountsDto,
    val personalColor: PersonalColorDto,
    val axes: List<RadarAxisDto> = emptyList() // レーダーも同時に欲しければ
)

@Serializable
data class ProfileDto(
    val userName: String = "ゲスト",
    val gender: Int? = null,
    val height: Int? = null,
    val weight: Int? = null
)

@Serializable
data class CountsDto(
    val itemCount: Int,
    val coordinateCount: Int,
    val favoriteCount: Int
)

@Serializable
data class PersonalColorDto(
    val colorName: String,
    val raw: Float
)

@Serializable
data class RadarAxisDto(
    val label: String,
    val norm01: Float
)
