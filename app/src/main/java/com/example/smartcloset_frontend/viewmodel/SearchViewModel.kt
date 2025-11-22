package com.example.smartcloset_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.SearchData
import com.example.smartcloset_frontend.data.repository.SearchRepository
import kotlinx.coroutines.launch

class SearchViewModel: ViewModel() {
    private val repository = SearchRepository()

    // 検索データをサーバーに送信する関数
    fun sendSearchData(
        query: String,
    ) {
        // viewModelScopeを使い、ViewModelのライフサイクルに連動したコルーチンを起動する
        viewModelScope.launch {
            try {
                // 送信するデータをSearchDataオブジェクトにまとめる
                val searchData = SearchData(
                    searchValue = query
                )
                // Retrofitクライアントを使って、サーバーにデータを送信する
                val response = repository.search(searchData)
                if (response.isSuccessful) {
                    // 通信が成功した場合の処理
                    println("Search data sent successfully")
                } else {
                    // サーバーがエラーレスポンスを返した場合の処理
                    println("Failed to send search data: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // 通信エラーやデータ変換エラーなど、例外が発生した場合の処理
                println("An error occurred: ${e.message}")


            }
        }
    }
}