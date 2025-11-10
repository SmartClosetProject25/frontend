package com.example.smartcloset_frontend.data.repository
import com.example.smartcloset_frontend.data.ProfileData
import com.example.smartcloset_frontend.network.RetrofitClient

class ProfileRepository {
    suspend fun updateProfile(profileData: ProfileData) =
        RetrofitClient.instance.updateProfile(profileData)
}