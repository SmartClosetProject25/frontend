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
import com.example.smartcloset_frontend.ui.networkErr.AsyncState
import com.example.smartcloset_frontend.ui.networkErr.launchWithAsyncState


class AddItemViewModel : ViewModel() {
    private val repository = AddDataRepository()

    var itemState by mutableStateOf(ItemFormState())
        private set

    // 登録処理の状態（Loading / Success / Error）
    var addItemState by mutableStateOf<AsyncState<Unit>>(AsyncState.Idle)
        private set

    fun setFormState(newState: ItemFormState) {
        itemState = newState
    }

    fun resetAddItemState() {
        addItemState = AsyncState.Idle
    }

    fun addItem(localPath: String, userId: Int) {
        viewModelScope.launch {
            addItemState = AsyncState.Loading
            try {
                // ★★★ 実際のサーバ登録処理 ★★★
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

                // ここまで来た＝例外は出てない → 成功
                addItemState = AsyncState.Success(Unit)

            } catch (e: Exception) {
                // ネットワーク系のエラーか判定
                val isNetwork = e is java.net.ConnectException ||
                        e is java.net.SocketTimeoutException ||
                        e is java.net.UnknownHostException

                val msg = if (isNetwork) {
                    "サーバーに接続できませんでした"
                } else {
                    "登録中にエラーが発生しました"
                }

                // 例外はここで「Error状態」に変換して UI に渡す
                addItemState = AsyncState.Error(
                    isNetworkError = isNetwork,
                    message = msg
                )
            }
        }
    }
}

