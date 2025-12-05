package com.example.smartcloset_frontend.data.repository
import android.util.Log
import com.example.smartcloset_frontend.data.GetItemsResponse
import com.example.smartcloset_frontend.data.ItemData
import com.example.smartcloset_frontend.data.JudgeRequestData
import com.example.smartcloset_frontend.network.RetrofitClient

class ItemRepository {
    suspend fun getItems(userId: Int): List<ItemData> {
        val response = RetrofitClient.instance.getItems(userId)
        return response.items
    }
    suspend fun getDetailItems(itemId: Int) =
        RetrofitClient.instance.getItemDetail(itemId)

    suspend fun judge(data: JudgeRequestData) =
        RetrofitClient.instance.judgement( data)
}