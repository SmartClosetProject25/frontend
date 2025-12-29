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

    suspend fun generateImage(imagePaths: List<String>, modelImageBase64: String? = null, modelTemplate: String? = null): Response<ImageResponse> {
        val request = GenerateImageRequest(
            image_paths = imagePaths,
            model_image_base64 = modelImageBase64,
            model_template = modelTemplate
        )
        return RetrofitClient.instance.generateImage(request)
    }
}
