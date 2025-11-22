package com.example.smartcloset_frontend.ui.networkErr

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// Throwable を AsyncState.Error に変換する拡張関数
fun Throwable.toAsyncErrorState(defaultMessage: String = "エラーが発生しました"): AsyncState.Error {
    val isNetwork = this is ConnectException ||
            this is SocketTimeoutException ||
            this is UnknownHostException

    val msg = if (isNetwork) {
        "サーバーに接続できませんでした"
    } else {
        defaultMessage
    }

    return AsyncState.Error(
        isNetworkError = isNetwork,
        message = msg
    )
}