package com.example.smartcloset_frontend.data.repository

import com.example.smartcloset_frontend.data.GenerateImageRequest
import com.example.smartcloset_frontend.data.ProposalResponse
import com.example.smartcloset_frontend.data.TodayPlanData
import com.example.smartcloset_frontend.network.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Response

class TodayPlanRepository {
    suspend fun sendTodayPlan(todayPlanData: TodayPlanData): Response<ProposalResponse> =
        RetrofitClient.instance.sendTodayPlan(todayPlanData)

    suspend fun generateImage(itemIds: List<String>): Response<ResponseBody> {
        val request = GenerateImageRequest(item_ids = itemIds)
        return RetrofitClient.instance.generateImage(request)
    }
}
