package com.example.smartcloset_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.LoginData
import com.example.smartcloset_frontend.data.repository.LoginRepository
import kotlinx.coroutines.launch

// LoginScreenのためのViewModel。UI関連のデータとロジックを管理する
class LoginViewModel : ViewModel() {
    private val repository = LoginRepository()
    
    // フォームのデータをサーバーに送信する関数
    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit  // コールバックを追加
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
                    onResult(true, null)
                } else {
                    // サーバーがエラーレスポンスを返した場合の処理
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = errorBody ?: "ログインに失敗しました。"
                    onResult(false, errorMessage)
                }
            } catch (e: Exception) {
                // 通信エラーやデータ変換エラーなど、例外が発生した場合の処理
                onResult(false, "ネットワークエラー: ${e.message}")
            }
        }
    }
    
    // 自動ログイン用の関数
    suspend fun autoLogin(email: String, password: String): Boolean {
        return try {
            val loginData = LoginData(email = email, password = password)
            val response = repository.login(loginData)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}