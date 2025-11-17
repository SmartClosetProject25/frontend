package com.example.smartcloset_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.smartcloset_frontend.data.repository.AddDataRepository
import com.example.smartcloset_frontend.data.AddItemData

class AddItemViewModel : ViewModel() {
    private val repository = AddDataRepository()

    fun addItem(color: String, category: String, imageUrl: String) {
        viewModelScope.launch {
            repository.addItem(
                AddItemData(
                    color = color.toInt(),
                    category = category.toInt(),
                    imageUrl = imageUrl,
                    pattern = 0,
                    size = 0,
                    brand = "",
                    userId = 0
                )
            )

        }
    }
}