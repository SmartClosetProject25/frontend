package com.example.smartcloset_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.repository.UserSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserSessionViewModel(
    private val repository: UserSessionRepository
) : ViewModel() {

    private val _userId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> = _userId

    init {
        // アプリ起動時に DataStore から読み込んで StateFlow に反映
        viewModelScope.launch {
            repository.userIdFlow.collect { id ->
                _userId.value = id
            }
        }
    }

    fun setUserId(id: Int?) {
        viewModelScope.launch {
            repository.setUserId(id)
        }
    }

    fun clear() {
        viewModelScope.launch {
            repository.clear()
        }
    }
}
