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
        password: String
    ) {
        // viewModelScopeを使い、ViewModelのライフサイクルに連動したコルーチンを起動する
        viewModelScope.launch {
            try {
                // 送信するデータをLoginDataオブジェクトにまとめる
                val loginData = LoginData(
                    email = email,
                    password = password
                )
                // Retrofitクライアントを使って、サーバーにデータを送信する}
                val response = repository.login(loginData)
                if (response.isSuccessful) {
                    // 通信が成功した場合の処理
                    println("Login successful")
                } else {
                    // サーバーがエラーレスポンスを返した場合の処理
                    println("Failed to login: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // 通信エラーやデータ変換エラーなど、例外が発生した場合の処理
                println("An error occurred: ${e.message}")
            }
        }
    }
}