package com.example.smartcloset_frontend.data.repository

import com.example.smartcloset_frontend.data.PasswordResetRequestData
import com.example.smartcloset_frontend.data.PasswordResetConfirmData
import com.example.smartcloset_frontend.network.RetrofitClient

class PasswordResetRepository {
    suspend fun requestPasswordReset(email: String) =
        RetrofitClient.instance.requestPasswordReset(PasswordResetRequestData(email))
    
    suspend fun confirmPasswordReset(token: String, newPassword: String) =
        RetrofitClient.instance.confirmPasswordReset(PasswordResetConfirmData(token, newPassword))
}
