package com.example.smartcloset_frontend.ui.networkErr

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// 任意の ViewModel から使える共通ヘルパー
fun <T> ViewModel.launchWithAsyncState(
    state: MutableState<AsyncState<T>>,
    defaultErrorMessage: String = "エラーが発生しました",
    block: suspend () -> T
) {
    viewModelScope.launch {
        state.value = AsyncState.Loading
        try {
            val result = block()
            state.value = AsyncState.Success(result)
        } catch (e: Exception) {
            state.value = e.toAsyncErrorState(defaultErrorMessage)
        }
    }
}