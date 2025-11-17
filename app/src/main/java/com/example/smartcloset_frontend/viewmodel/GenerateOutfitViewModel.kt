package com.example.smartcloset_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.GenerateOutfitData
import com.example.smartcloset_frontend.data.ItemData
import com.example.smartcloset_frontend.data.repository.GenerateOutfitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GenerateOutfitViewModel(
    private val repository: GenerateOutfitRepository = GenerateOutfitRepository()
) : ViewModel() {

    private val _outfits = MutableStateFlow<List<ItemData>>(emptyList())
    val outfits: StateFlow<List<ItemData>> = _outfits

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun generateOutfit(plan: String, temperature: Int, cor: Int) {
        viewModelScope.launch {
            val generateOutfitData = GenerateOutfitData(plan, temperature, cor)
            repository.generateOutfit(generateOutfitData)
                .onSuccess {
                    _outfits.value = it
                }
                .onFailure {
                    _error.value = "Failed to generate outfit: ${it.message}"
                }
        }
    }
}
