package com.example.smartcloset_frontend.data.repository

import com.example.smartcloset_frontend.data.GenerateImageRequest
import com.example.smartcloset_frontend.data.ImageResponse
import com.example.smartcloset_frontend.data.ProposalResponse
import com.example.smartcloset_frontend.data.TodayPlanData
import com.example.smartcloset_frontend.network.RetrofitClient
import retrofit2.Response

class TodayPlanRepository {
    suspend fun sendTodayPlan(todayPlanData: TodayPlanData): Response<ProposalResponse> =
        RetrofitClient.instance.sendTodayPlan(todayPlanData)

    suspend fun generateImage(imagePaths: List<String>): Response<ImageResponse> {
        val request = GenerateImageRequest(image_paths = imagePaths)
        return RetrofitClient.instance.generateImage(request)
    }
}
