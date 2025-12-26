package com.example.smartcloset_frontend.data.repository
import com.example.smartcloset_frontend.data.GetProfileResponse
import com.example.smartcloset_frontend.data.ProfileData
import com.example.smartcloset_frontend.network.RetrofitClient

class ProfileRepository {
    suspend fun updateProfile(profileData: ProfileData) =
        RetrofitClient.instance.updateProfile(profileData)
    suspend fun fetchProfileSummary(userId: Int): Result<GetProfileResponse> = runCatching {
        val res = RetrofitClient.instance.getProfile(userId)
        if (!res.ok) error("server returned ok=false")
        res
    }
}