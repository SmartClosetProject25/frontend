package com.example.smartcloset_frontend.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.GenerateOutfitWithWeather
import com.example.smartcloset_frontend.data.repository.GenerateOutfitRepository
import kotlinx.coroutines.launch

class GenerateOutfitWithWeatherViewModel:ViewModel(){
    private val repository = GenerateOutfitRepository()

    var generatedImageResult: Result<Bitmap>? by mutableStateOf(null)
    var isLoading by mutableStateOf(false)

    fun generateOutfitWithWeather(data: GenerateOutfitWithWeather) {
        viewModelScope.launch {
            isLoading = true
            generatedImageResult = repository.generateOutfitWithWeather(data)
            isLoading = false
        }
    }
}