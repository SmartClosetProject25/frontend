package com.example.smartcloset_frontend.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.GenerateOutfitData
import com.example.smartcloset_frontend.data.repository.GenerateOutfitRepository
import kotlinx.coroutines.launch

class GenerateOutfitViewModel(
    private val repository: GenerateOutfitRepository
) : ViewModel() {
    var generatedImageResult: Result<Bitmap>? by mutableStateOf(null)
    var isLoading by mutableStateOf(false)

    fun generateOutfit(data: GenerateOutfitData) {
        viewModelScope.launch {
            isLoading = true
            generatedImageResult = repository.generateOutfit(data)
            isLoading = false
        }
    }
}
