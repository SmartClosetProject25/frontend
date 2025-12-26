package com.example.smartcloset_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.GetCoordinatesResponse
import com.example.smartcloset_frontend.data.repository.GetCoordinatesRepository
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
            repository.getCoordinates()
                .onSuccess {
                    _coordinatesData.value = it
                }
                .onFailure {
                    _error.value = "コーディネート履歴の取得に失敗しました: ${it.message}"
                }
            _isLoading.value = false
        }
    }
}

