package com.example.smartcloset_frontend.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartcloset_frontend.data.repository.ServerConfigRepository

class ServerConfigViewModelFactory(
    private val repo: ServerConfigRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServerConfigViewModel::class.java)) {
            return ServerConfigViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
