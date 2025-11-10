package com.example.smartcloset_frontend.data.repository
import com.example.smartcloset_frontend.data.SearchData
import com.example.smartcloset_frontend.network.RetrofitClient

class SearchRepository {
    suspend fun search(searchData: SearchData ) =
        RetrofitClient.instance.search(searchData)
}