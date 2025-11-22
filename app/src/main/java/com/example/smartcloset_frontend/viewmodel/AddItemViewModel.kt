package com.example.smartcloset_frontend.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.smartcloset_frontend.data.repository.AddDataRepository
import com.example.smartcloset_frontend.data.AddItemData
import com.example.smartcloset_frontend.ui.ItemFormState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class AddItemViewModel : ViewModel() {
    private val repository = AddDataRepository()

    // 画面間で共有するフォーム状態
    var itemState by mutableStateOf(ItemFormState())
        private set

    // 登録画面からフォーム内容をセット
    fun setFormState(newState: ItemFormState) {
        itemState = newState
    }

    fun addItem(localPath: String, userId: Int) {
        viewModelScope.launch {
            repository.addItem(
                AddItemData(
                    color = itemState.color,
                    pattern = itemState.pattern,
                    size = itemState.size,
                    brand = itemState.brand,
                    category = itemState.category,
                    userId = userId,
                    imageUrl = localPath,
                    material = itemState.material,
                    feature = itemState.feature,
                    season = itemState.season,
                    taste = itemState.taste,
                    itemName = itemState.itemName
                )
            )
        }
    }
}