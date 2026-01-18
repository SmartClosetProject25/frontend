package com.example.smartcloset_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.ProfileData
import com.example.smartcloset_frontend.data.repository.ProfileRepository
import com.example.smartcloset_frontend.ui.networkErr.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



// ProfileEditScreenのためのViewModel。UI関連のデータとロジックを管理する
class ProfileEditViewModel : ViewModel() {
    private val repository = ProfileRepository()

    // フォームのデータをサーバーに送信する関数
    fun updateProfile(
        userId: Int?,
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
                    userId = userId,
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

class ProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun load(userId: Int) {
        if (userId <= 0) {
            _uiState.value = ProfileUiState.Error("userId が不正です")
            return
        }

        _uiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            repository.fetchProfileSummary(userId)
                .onSuccess { data ->
                    _uiState.value = ProfileUiState.Success(data)
                }
                .onFailure { e ->
                    _uiState.value = ProfileUiState.Error(e.message ?: "取得に失敗しました")
                }
        }
    }

    fun retry(userId: Int) = load(userId)
}

class ProfileViewModelFactory(
    private val repository: ProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
