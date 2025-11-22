package com.example.smartcloset_frontend.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.ItemDetailData
import com.example.smartcloset_frontend.data.repository.ItemRepository
import com.example.smartcloset_frontend.ui.networkErr.AsyncState
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue

class ClothesDetailViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ItemRepository()

    var detailState by mutableStateOf<AsyncState<ItemDetailData>>(AsyncState.Idle)
        private set

    fun loadDetail(itemId: Int) {
        // すでに成功状態で同じIDなら再取得しない、などしてもOK
        viewModelScope.launch {
            detailState = AsyncState.Loading
            try {
                val detail = repository.getDetailItems(itemId)
                detailState = AsyncState.Success(detail)
            } catch (e: Exception) {
                val isNetwork = e is java.net.ConnectException ||
                        e is java.net.SocketTimeoutException ||
                        e is java.net.UnknownHostException

                val msg = if (isNetwork) "サーバーに接続できませんでした"
                else "詳細取得中にエラーが発生しました"

                detailState = AsyncState.Error(
                    isNetworkError = isNetwork,
                    message = msg
                )
            }
        }
    }
}