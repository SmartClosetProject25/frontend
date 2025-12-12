package com.example.smartcloset_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.LoginData
import com.example.smartcloset_frontend.data.LoginResponse
import com.example.smartcloset_frontend.data.repository.AuthRepository
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@Serializable
data class ErrorResponse(
    val error: String? = null,
    val message: String? = null
)

// LoginScreenのためのViewModel。UI関連のデータとロジックを管理する
class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val json = Json { ignoreUnknownKeys = true }
    
    // エラーメッセージを日本語に変換
    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody == null) {
            return "ログインに失敗しました。"
        }
        
        return try {
            val errorResponse = json.decodeFromString<ErrorResponse>(errorBody)
            val errorText = errorResponse.error ?: errorResponse.message ?: errorBody
            
            // 英語のエラーメッセージを日本語に変換
            when {
                errorText.contains("email and password are required", ignoreCase = true) -> 
                    "メールアドレスとパスワードを入力してください。"
                errorText.contains("Invalid email or password", ignoreCase = true) -> 
                    "メールアドレスまたはパスワードが正しくありません。"
                errorText.contains("email is required", ignoreCase = true) -> 
                    "メールアドレスを入力してください。"
                errorText.contains("password is required", ignoreCase = true) -> 
                    "パスワードを入力してください。"
                else -> errorText
            }
        } catch (e: Exception) {
            // JSONパースに失敗した場合は、そのまま返すかデフォルトメッセージ
            if (errorBody.length < 200) {
                errorBody
            } else {
                "ログインに失敗しました。"
            }
        }
    }
    
    // フォームのデータをサーバーに送信する関数
    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?, Int?) -> Unit  // コールバックにuser_idを追加
    ) {
        // viewModelScopeを使い、ViewModelのライフサイクルに連動したコルーチンを起動する
        viewModelScope.launch {
            try {
                // 送信するデータをLoginDataオブジェクトにまとめる
                val loginData = LoginData(
                    email = email,
                    password = password
                )
                // Retrofitクライアントを使って、サーバーにデータを送信する
                val response = repository.login(loginData)
                if (response.isSuccessful) {
                    // 通信が成功した場合の処理
                    val loginResponse = response.body()
                    val userId = loginResponse?.userId
                    onResult(true, null, userId)
                } else {
                    // サーバーがエラーレスポンスを返した場合の処理
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = parseErrorMessage(errorBody)
                    onResult(false, errorMessage, null)
                }
            } catch (e: Exception) {
                // 通信エラーやデータ変換エラーなど、例外が発生した場合の処理
                onResult(false, "ネットワークエラー: ${e.message ?: "接続できませんでした"}", null)
            }
        }
    }
    
    // 自動ログイン用の関数（成功時はuserIdを返す）
    suspend fun autoLogin(email: String, password: String): Int? {
        return try {
            val loginData = LoginData(email = email, password = password)
            val response = repository.login(loginData)
            if (response.isSuccessful) {
                val loginResponse = response.body()
                loginResponse?.userId
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}