package com.example.smartcloset_frontend.data.repository
import com.example.smartcloset_frontend.network.RetrofitClient

class ItemRepository {
    suspend fun getItems(userId: Int) =
        RetrofitClient.instance.getItems(1)


    suspend fun getDetailItems(userId: Int) =
        RetrofitClient.instance.getItemDetail(1)
}