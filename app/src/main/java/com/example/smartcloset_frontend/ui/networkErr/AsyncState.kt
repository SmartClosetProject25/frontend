package com.example.smartcloset_frontend.ui.networkErr

// 非同期処理の状態
//くるくる回ってる、成功、失敗、などを表す
sealed class AsyncState<out T> {
    object Idle : AsyncState<Nothing>()
    object Loading : AsyncState<Nothing>()
    data class Success<T>(val data: T? = null) : AsyncState<T>()
    data class Error(
        val isNetworkError: Boolean,
        val message: String? = null
    ) : AsyncState<Nothing>()
}