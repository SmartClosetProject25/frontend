package com.example.smartcloset_frontend.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.ItemData
import com.example.smartcloset_frontend.data.repository.ItemRepository
import com.example.smartcloset_frontend.ui.networkErr.AsyncState


class ItemViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ItemRepository()
    var items by mutableStateOf<List<ItemData>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var itemListState by mutableStateOf<AsyncState<List<ItemData>>>(AsyncState.Idle)
        private set

    fun loadItems(userId: Int, forceRefresh: Boolean = false) {
        // すでに成功済みで、更新がいらないならAPIを叩かない
        if (!forceRefresh && itemListState is AsyncState.Success) return

        viewModelScope.launch {
            itemListState = AsyncState.Loading
            try {
                val items = repository.getItems(userId)
                itemListState = AsyncState.Success(items)
            } catch (e: Exception) {
                itemListState = AsyncState.Error(
                    isNetworkError = e is java.net.ConnectException ||
                            e is java.net.SocketTimeoutException ||
                            e is java.net.UnknownHostException,
                    message = "一覧取得でエラーが発生しました"
                )
            }
        }
    }
}