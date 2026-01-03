package com.example.smartcloset_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.GetCoordinatesResponse
import com.example.smartcloset_frontend.data.repository.GetCoordinatesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SuggestionHistoryViewModel(
    private val repository: GetCoordinatesRepository = GetCoordinatesRepository()
) : ViewModel() {
    private val _coordinatesData = MutableStateFlow<GetCoordinatesResponse?>(null)
    val coordinatesData: StateFlow<GetCoordinatesResponse?> = _coordinatesData

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchCoordinates() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val maxRetries = 3
            var retryCount = 0
            var lastError: Throwable? = null
            
            while (retryCount < maxRetries) {
                val result = repository.getCoordinates()
                
                result.onSuccess {
                    _coordinatesData.value = it
                    _isLoading.value = false
                    return@launch
                }.onFailure { error ->
                    lastError = error
                    retryCount++
                    
                    // "unexpected end of stream"エラーの場合はリトライ
                    val errorMessage = error.message ?: ""
                    val isRetryableError = errorMessage.contains("unexpected end of stream", ignoreCase = true) ||
                            errorMessage.contains("stream", ignoreCase = true) ||
                            errorMessage.contains("connection", ignoreCase = true) ||
                            errorMessage.contains("timeout", ignoreCase = true)
                    
                    if (retryCount < maxRetries && isRetryableError) {
                        // リトライ前に待機（指数バックオフ）
                        val delayMs = (1000L * retryCount).coerceAtMost(3000L)
                        delay(delayMs)
                    } else {
                        // リトライ不可能または最大リトライ回数に達した
                        _error.value = if (retryCount >= maxRetries) {
                            "コーディネート履歴の取得に失敗しました（${maxRetries}回試行しました）: ${error.message}"
                        } else {
                            "コーディネート履歴の取得に失敗しました: ${error.message}"
                        }
                        _isLoading.value = false
                        return@launch
                    }
                }
            }
            
            // ここに到達した場合は全てのリトライが失敗
            _error.value = "コーディネート履歴の取得に失敗しました（${maxRetries}回試行しました）: ${lastError?.message ?: "不明なエラー"}"
            _isLoading.value = false
        }
    }
}

