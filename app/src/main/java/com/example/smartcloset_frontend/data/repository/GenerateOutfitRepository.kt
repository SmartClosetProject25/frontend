package com.example.smartcloset_frontend.data.repository
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.smartcloset_frontend.network.RetrofitClient
import com.example.smartcloset_frontend.data.GenerateOutfitData

class GenerateOutfitRepository {

    suspend fun generateOutfit(data: GenerateOutfitData): Result<Bitmap> {
        return try {

            // DTO に変換
            val request = GenerateOutfitData(
                userId = data.userId,
                selfieId = data.selfieId,
                topsId = data.topsId,
                bottomsId = data.bottomsId,
                othersId = data.othersId,
                others2Id = data.others2Id
            )

            val response = RetrofitClient.instance.generateOutfit(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val bytes = body.bytes()
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    Result.success(bmp)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
