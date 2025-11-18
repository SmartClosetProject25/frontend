package com.example.smartcloset_frontend.data.repository
import com.example.smartcloset_frontend.network.RetrofitClient
import com.example.smartcloset_frontend.data.GenerateOutfitData
import com.example.smartcloset_frontend.data.ItemData

class GenerateOutfitRepository {
    suspend fun generateOutfit(generateOutfitData: GenerateOutfitData): Result<List<ItemData>> {
        return try {
            val response = RetrofitClient.instance.generateOutfit(generateOutfitData)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}