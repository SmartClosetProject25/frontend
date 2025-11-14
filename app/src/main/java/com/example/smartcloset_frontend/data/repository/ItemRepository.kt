package com.example.smartcloset_frontend.data.repository
import com.example.smartcloset_frontend.network.RetrofitClient

class ItemRepository {
    suspend fun getItems() =
        RetrofitClient.instance.getItems()
}