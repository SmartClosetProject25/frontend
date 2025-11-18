package com.example.smartcloset_frontend.data
import kotlinx.serialization.Serializable

// パスワードリセットリクエスト用のデータクラス
@Serializable
data class PasswordResetRequestData(
    val email: String
)
