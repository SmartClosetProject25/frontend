package com.example.smartcloset_frontend.data.repository

import com.example.smartcloset_frontend.data.GetCoordinatesResponse
import com.example.smartcloset_frontend.network.RetrofitClient
import java.io.IOException

class GetCoordinatesRepository {
    suspend fun getCoordinates(): Result<GetCoordinatesResponse> {
        return runCatching {
            try {
                val response = RetrofitClient.instance.getCoordinates()
                if (response.isSuccessful) {
                    response.body() ?: throw IOException("Response body is null")
                } else {
                    throw IOException("HTTP Error: ${response.code()} ${response.message()}")
                }
            } catch (e: IOException) {
                // ネットワークエラーやストリームエラーを再スロー
                throw e
            } catch (e: Exception) {
                // その他の予期しないエラー
                throw IOException("予期しないエラーが発生しました: ${e.message}", e)
            }
        }
    }
}

