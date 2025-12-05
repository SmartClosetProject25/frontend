package com.example.smartcloset_frontend.data.repository

import com.example.smartcloset_frontend.network.RetrofitClient

class MasterDataRepository {
    suspend fun getMasterData() = RetrofitClient.instance.getMasterData()
}

