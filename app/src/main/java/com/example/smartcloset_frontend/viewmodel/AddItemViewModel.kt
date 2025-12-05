package com.example.smartcloset_frontend.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.smartcloset_frontend.data.repository.AddDataRepository
import com.example.smartcloset_frontend.ui.ItemFormState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.smartcloset_frontend.ui.networkErr.AsyncState
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class AddItemViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = AddDataRepository()

    // 入力フォームの状態
    var itemState by mutableStateOf(ItemFormState())
        private set

    // 登録処理の状態（Idle / Loading / Success / Error）
    var addItemState by mutableStateOf<AsyncState<Unit>>(AsyncState.Idle)
        private set

    fun setFormState(newState: ItemFormState) {
        itemState = newState
    }

    fun resetAddItemState() {
        addItemState = AsyncState.Idle
    }

    /**
     * 渡された imageUri からそのまま Multipart を作ってサーバに送る
     */
    fun addItem(imageUri: Uri, userId: Int) {
        viewModelScope.launch {
            addItemState = AsyncState.Loading

            try {
                // Application コンテキスト取得
                val context = getApplication<Application>()
                val cr = context.contentResolver

                // URI からバイト列を取得
                val bytes = cr.openInputStream(imageUri)?.use { it.readBytes() }
                    ?: throw IOException("画像を読み込めませんでした") as Throwable

                // ContentResolver から MIME type を取得（取れなければ image/*）
                val mediaType = cr.getType(imageUri)?.toMediaTypeOrNull()
                    ?: "image/*".toMediaTypeOrNull()

                val requestBody = bytes.toRequestBody(mediaType)

                // サーバ側で保存するファイル名（お好みで変更可）
                val fileName = "item_${System.currentTimeMillis()}.jpg"

                // MultipartBody.Part 作成（"file" はサーバ側のフィールド名に合わせる）
                val imagePart = MultipartBody.Part.createFormData(
                    "image",
                    fileName,
                    requestBody
                )

                // Repository 経由でサーバへ登録
                repository.addItem(
                    itemState = itemState,
                    imagePart = imagePart,
                    userId = userId
                )

                // ここまで例外が出なければ成功
                addItemState = AsyncState.Success(Unit)

            } catch (e: Exception) {
                // ネットワーク系エラー判定
                val isNetwork = e is java.net.ConnectException ||
                        e is java.net.SocketTimeoutException ||
                        e is java.net.UnknownHostException

                val msg = if (isNetwork) {
                    "サーバーに接続できませんでした"
                } else {
                    "登録中にエラーが発生しました"
                }

                addItemState = AsyncState.Error(
                    isNetworkError = isNetwork,
                    message = msg
                )
            }
        }
    }
}

