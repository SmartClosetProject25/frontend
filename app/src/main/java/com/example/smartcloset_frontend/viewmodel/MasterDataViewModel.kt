package com.example.smartcloset_frontend.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.smartcloset_frontend.data.repository.MasterDataRepository

class MasterDataViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MasterDataRepository()
    
    var categoryMap by mutableStateOf<Map<Int, String>>(emptyMap())
        private set
    var sizeMap by mutableStateOf<Map<Int, String>>(emptyMap())
        private set
    var colorMap by mutableStateOf<Map<Int, String>>(emptyMap())
        private set
    var patternMap by mutableStateOf<Map<Int, String>>(emptyMap())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    init {
        loadMasterData()
    }
    
    fun loadMasterData() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = repository.getMasterData()
                if (response.isSuccessful && response.body()?.status == "ok") {
                    val data = response.body()!!
                    categoryMap = (data.categories ?: emptyMap()).mapKeys { it.key.toInt() }
                    sizeMap = (data.sizes ?: emptyMap()).mapKeys { it.key.toInt() }
                    colorMap = (data.colors ?: emptyMap()).mapKeys { it.key.toInt() }
                    patternMap = (data.patterns ?: emptyMap()).mapKeys { it.key.toInt() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}

