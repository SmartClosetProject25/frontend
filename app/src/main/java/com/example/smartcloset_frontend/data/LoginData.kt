package com.example.smartcloset_frontend.data
import kotlinx.serialization.Serializable

// ユーザーのログイン情報を表すデータクラス
//サインアップにも同じデータを使用
@Serializable
data class LoginData(
    val email: String,
    val password: String
)