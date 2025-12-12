package com.example.smartcloset_frontend.data.repository

import com.example.smartcloset_frontend.network.RetrofitClient
import com.example.smartcloset_frontend.ui.ItemFormState
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody
import java.io.IOException

class AddDataRepository {
    suspend fun addItem(
        itemState: ItemFormState,
        imagePart: MultipartBody.Part,
        userId: Int,
    ) {
        // --- 各フィールドを RequestBody に変換 ---
        val textMediaType = "text/plain".toMediaTypeOrNull()

        val userIdBody: RequestBody =
            userId.toString().toRequestBody(textMediaType)

        val itemNameBody: RequestBody =
            itemState.itemName.toRequestBody(textMediaType)

        val colorBody: RequestBody =
            itemState.color.toString().toRequestBody(textMediaType)

        val patternBody: RequestBody =
            itemState.pattern.toString().toRequestBody(textMediaType)

        val sizeBody: RequestBody =
            itemState.size.toString().toRequestBody(textMediaType)

        val brandBody: RequestBody =
            itemState.brand.toRequestBody(textMediaType)

        val categoryBody: RequestBody =
            itemState.category.toString().toRequestBody(textMediaType)

        val materialBody: RequestBody =
            itemState.material.toRequestBody(textMediaType)

        val featureBody: RequestBody =
            itemState.feature.toRequestBody(textMediaType)

        val seasonBody: RequestBody =
            itemState.season.toString().toRequestBody(textMediaType)

        val tasteBody: RequestBody =
            itemState.taste.toString().toRequestBody(textMediaType)

        // --- Retrofit の Multipart API を呼び出す ---
        RetrofitClient.instance.addItem(
            userId = userIdBody,
            itemName = itemNameBody,
            color = colorBody,
            pattern = patternBody,
            size = sizeBody,
            brand = brandBody,
            category = categoryBody,
            material = materialBody,
            feature = featureBody,
            season = seasonBody,image = imagePart,
            taste = tasteBody,
        )
    }

    suspend fun updateItem(
        itemId: Int,
        itemState: ItemFormState,
        imagePart: MultipartBody.Part?,
        userId: Int
    ) {
        val textMediaType = "text/plain".toMediaTypeOrNull()

        val itemIdBody: RequestBody = itemId.toString().toRequestBody(textMediaType)
        val userIdBody: RequestBody = userId.toString().toRequestBody(textMediaType)
        val itemNameBody: RequestBody = itemState.itemName.toRequestBody(textMediaType)
        val colorBody: RequestBody = itemState.color.toString().toRequestBody(textMediaType)
        val patternBody: RequestBody = itemState.pattern.toString().toRequestBody(textMediaType)
        val sizeBody: RequestBody = itemState.size.toString().toRequestBody(textMediaType)
        val brandBody: RequestBody = itemState.brand.toRequestBody(textMediaType)
        val categoryBody: RequestBody = itemState.category.toString().toRequestBody(textMediaType)
        val materialBody: RequestBody = itemState.material.toRequestBody(textMediaType)
        val featureBody: RequestBody = itemState.feature.toRequestBody(textMediaType)
        val seasonBody: RequestBody = itemState.season.toString().toRequestBody(textMediaType)
        val tasteBody: RequestBody = itemState.taste.toString().toRequestBody(textMediaType)

        val response = RetrofitClient.instance.updateItem(
            itemId = itemIdBody,
            userId = userIdBody,
            itemName = itemNameBody,
            color = colorBody,
            pattern = patternBody,
            size = sizeBody,
            brand = brandBody,
            category = categoryBody,
            material = materialBody,
            feature = featureBody,
            season = seasonBody,
            taste = tasteBody,
            image = imagePart
        )

        if (!response.isSuccessful) {
            throw IOException("更新に失敗しました: ${response.message()}")
        }
    }
}