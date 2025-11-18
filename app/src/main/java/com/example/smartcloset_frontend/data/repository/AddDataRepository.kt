package com.example.smartcloset_frontend.data.repository

import com.example.smartcloset_frontend.data.AddItemData
import com.example.smartcloset_frontend.network.RetrofitClient

class AddDataRepository {
    suspend fun addItem(addItemData: AddItemData) =
        RetrofitClient.instance.addItem(addItemData)
}