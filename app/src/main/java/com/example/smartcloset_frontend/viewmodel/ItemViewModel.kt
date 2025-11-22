package com.example.smartcloset_frontend.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.ItemData
import com.example.smartcloset_frontend.data.JudgeRequestData
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
    var judgeError by mutableStateOf<String?>(null)
        private set

    fun clearJudgeError() {
        judgeError = null
    }

    fun sendJudge(data: JudgeRequestData) {
        viewModelScope.launch {
            try {
                repository.judge(data)   // ← Retrofit の suspend 関数想定
                judgeError = null        // 成功したらエラーを消す
            } catch (e: Exception) {
                // ネットワーク系のエラーか判定（お好み）
                val isNetwork = e is java.net.ConnectException ||
                        e is java.net.SocketTimeoutException ||
                        e is java.net.UnknownHostException

                judgeError = if (isNetwork) {
                    "サーバーに接続できませんでした"
                } else {
                    "評価送信中にエラーが発生しました"
                }

                // Log だけして落とさない
                e.printStackTrace()
            }
        }
    }
}