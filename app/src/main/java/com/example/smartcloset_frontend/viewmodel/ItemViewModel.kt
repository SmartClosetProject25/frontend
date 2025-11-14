package com.example.smartcloset_frontend.viewmodel
import androidx.lifecycle.ViewModel
import com.example.smartcloset_frontend.data.ItemData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.repository.ItemRepository


class ItemViewModel(
    private val repository: ItemRepository = ItemRepository()
) : ViewModel() {

    private val _items = MutableStateFlow<List<ItemData>>(emptyList())
    val items: StateFlow<List<ItemData>> = _items

    private var loaded = false

    fun loadIfNeeded() {
        if (loaded) return

        viewModelScope.launch {
            val result = repository.getItems()
            _items.value = result
            loaded = true
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _items.value = repository.getItems()
        }
    }
}