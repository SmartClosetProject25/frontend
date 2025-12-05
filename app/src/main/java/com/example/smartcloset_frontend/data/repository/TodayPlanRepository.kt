package com.example.smartcloset_frontend.data.repository
import com.example.smartcloset_frontend.data.ProposalResponse
import com.example.smartcloset_frontend.data.TodayPlanData
import com.example.smartcloset_frontend.network.RetrofitClient
import retrofit2.Response

class TodayPlanRepository {
    suspend fun sendTodayPlan(todayPlanData: TodayPlanData): Response<ProposalResponse> =
        RetrofitClient.instance.sendTodayPlan(todayPlanData)
}

