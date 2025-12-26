package com.example.smartcloset_frontend.data.repository

import com.example.smartcloset_frontend.data.GetCoordinatesResponse
import com.example.smartcloset_frontend.network.RetrofitClient

class GetCoordinatesRepository {
    suspend fun getCoordinates(): Result<GetCoordinatesResponse> {
        return runCatching {
            val response = RetrofitClient.instance.getCoordinates()
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Response body is null")
            } else {
                throw Exception("Error: ${response.code()} ${response.message()}")
            }
        }
    }
}

