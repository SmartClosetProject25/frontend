package com.example.smartcloset_frontend.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.ItemData
import com.example.smartcloset_frontend.data.JudgeRequestData
import com.example.smartcloset_frontend.data.repository.ItemRepository
import com.example.smartcloset_frontend.ui.networkErr.AsyncState
import kotlinx.coroutines.launch

class ItemViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ItemRepository()

    // UI が直接参照する一覧キャッシュ
    var items by mutableStateOf<List<ItemData>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var itemListState by mutableStateOf<AsyncState<List<ItemData>>>(AsyncState.Idle)
        private set

    fun loadItems(userId: Int, forceRefresh: Boolean = false) {
        // すでに成功済み & items も入っているなら再取得しない（キャッシュ利用）
        if (!forceRefresh && itemListState is AsyncState.Success && items.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            itemListState = AsyncState.Loading

            try {
                // サーバーから取得
                val newItems = repository.getItems(userId)

                // 成功したら UI 用キャッシュを更新
                items = newItems

                // 状態も Success にしておく
                itemListState = AsyncState.Success(newItems)

            } catch (e: Exception) {
                // ネットワークエラーか判定
                val isNetwork = e is java.net.ConnectException ||
                        e is java.net.SocketTimeoutException ||
                        e is java.net.UnknownHostException

                // エラー文言だけ更新。items は触らない
                errorMessage = if (isNetwork) {
                    "サーバーに接続できませんでした"
                } else {
                    "一覧取得でエラーが発生しました (${e.message})"
                }

                itemListState = AsyncState.Error(
                    isNetworkError = isNetwork,
                    message = errorMessage
                )
            } finally {
                isLoading = false
            }
        }
    }

    var judgeError by mutableStateOf<String?>(null)
        private set

    fun clearJudgeError() {
        judgeError = null
    }

    fun sendJudge(data: JudgeRequestData) {
        viewModelScope.launch {
            try {
                repository.judge(data)
                judgeError = null
            } catch (e: Exception) {
                val isNetwork = e is java.net.ConnectException ||
                        e is java.net.SocketTimeoutException ||
                        e is java.net.UnknownHostException

                judgeError = if (isNetwork) {
                    "サーバーに接続できませんでした"
                } else {
                    "評価送信中にエラーが発生しました"
                }
                e.printStackTrace()
            }
        }
    }

    fun toggleFavoriteOnServer(userId: Int, itemId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                repository.setFavorite(userId, itemId, isFavorite)
            } catch (e: Exception) {
                // TODO: エラー時の処理（ログ出す・スナックバー出すなど）
                e.printStackTrace()
            }
        }
    }
}
