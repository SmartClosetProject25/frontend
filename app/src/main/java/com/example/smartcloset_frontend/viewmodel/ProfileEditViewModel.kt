package com.example.smartcloset_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.ProfileData
import com.example.smartcloset_frontend.data.repository.ProfileRepository
import kotlinx.coroutines.launch


// ProfileEditScreenのためのViewModel。UI関連のデータとロジックを管理する
class ProfileEditViewModel : ViewModel() {
    private val repository = ProfileRepository()

    // フォームのデータをサーバーに送信する関数
    fun updateProfile(
        name: String,
        gender: String,
        height: String,
        weight: String,
        personalColor: String,
        skeleton: String
    ) {
        // viewModelScopeを使い、ViewModelのライフサイクルに連動したコルーチンを起動する
        viewModelScope.launch {
            try {
                // 送信するデータをProfileDataオブジェクトにまとめる
                val profileData = ProfileData(
                    name = name,
                    gender = gender,
                    height = height.toInt(),
                    weight = weight.toInt(),
                    personalColor = personalColor,
                    skeleton = skeleton
                )
                // Retrofitクライアントを使って、サーバーにデータを送信する
                val response = repository.updateProfile(profileData)

                if (response.isSuccessful) {
                    // 通信が成功した場合の処理
                    println("Profile updated successfully")
                } else {
                    // サーバーがエラーレスポンスを返した場合の処理
                    println("Failed to update profile: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // 通信エラーやデータ変換エラーなど、例外が発生した場合の処理
                println("An error occurred: ${e.message}")
            }
        }
    }
}
